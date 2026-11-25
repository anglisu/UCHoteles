package es.unican.ps.uchoteles.businessLayer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import es.unican.ps.uchoteles.common.businessLayer.IGestionReservas;
import es.unican.ps.uchoteles.common.daoLayer.IHotelesDAO;
import es.unican.ps.uchoteles.common.daoLayer.IReservasDAO;
import es.unican.ps.uchoteles.common.domain.DatosCliente;
import es.unican.ps.uchoteles.common.domain.DatosPago;
import es.unican.ps.uchoteles.common.domain.DisponibilidadHabitacion;
import es.unican.ps.uchoteles.common.domain.EstadoReserva;
import es.unican.ps.uchoteles.common.domain.Habitacion;
import es.unican.ps.uchoteles.common.domain.Hotel;
import es.unican.ps.uchoteles.common.domain.Reserva;
import es.unican.ps.uchoteles.common.domain.ReservaTipoHabitacion;
import es.unican.ps.uchoteles.common.domain.TipoHabitacion;
import es.unican.ps.uchoteles.common.exceptions.CancelacionNoPermitidaException;
import es.unican.ps.uchoteles.common.exceptions.HabitacionNoDisponibleException;
import es.unican.ps.uchoteles.common.exceptions.ReservaCaducadaException;
import es.unican.ps.uchoteles.common.exceptions.ReservaNoAccesibleException;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;

/**
 * Lógica de los casos de uso Reservar, Consultar reserva y Cancelar reserva.
 *
 * <p>El bloqueo de 10 minutos se implementa guardando la reserva como PENDIENTE
 * (cuenta como ocupada) y programando un temporizador no persistente que la
 * elimina si para entonces no se ha confirmado.</p>
 */
@Stateless
public class GestionReservas implements IGestionReservas {

    /** Minutos que se mantienen bloqueadas las habitaciones a la espera de confirmar. */
    static final int TIEMPO_BLOQUEO_MIN = 10;

    /** Días mínimos de antelación respecto a la fecha de entrada para poder cancelar. */
    static final int LIMITE_CANCELACION_DIAS = 2;

    @EJB
    private IHotelesDAO hotelesDAO;

    @EJB
    private IReservasDAO reservasDAO;

    @Resource
    private TimerService timerService;

    @Override
    public List<DisponibilidadHabitacion> consultarDisponibilidad(Long idHotel, LocalDate fechaEntrada,
                                                                  LocalDate fechaSalida) {
        List<DisponibilidadHabitacion> disponibilidad = new ArrayList<>();
        if (idHotel == null || !fechasValidas(fechaEntrada, fechaSalida)) {
            return disponibilidad;
        }
        Hotel hotel = hotelesDAO.hotel(idHotel);
        if (hotel == null) {
            return disponibilidad;
        }
        for (Habitacion h : hotel.getHabitaciones()) {
            disponibilidad.add(new DisponibilidadHabitacion(h.getTipo(),
                    calcularLibres(h, fechaEntrada, fechaSalida), h.getPrecioPorNoche()));
        }
        return disponibilidad;
    }

    @Override
    public Reserva bloquearHabitaciones(Long idHotel, LocalDate fechaEntrada, LocalDate fechaSalida,
                                        Map<TipoHabitacion, Integer> habitaciones)
            throws HabitacionNoDisponibleException {
        if (idHotel == null || !fechasValidas(fechaEntrada, fechaSalida) || habitaciones == null) {
            throw new IllegalArgumentException("Datos de la reserva no válidos");
        }
        Hotel hotel = hotelesDAO.hotel(idHotel);
        if (hotel == null) {
            throw new IllegalArgumentException("No existe el hotel " + idHotel);
        }

        Reserva reserva = new Reserva(hotel, fechaEntrada, fechaSalida, EstadoReserva.PENDIENTE);
        for (Map.Entry<TipoHabitacion, Integer> pedido : habitaciones.entrySet()) {
            int n = pedido.getValue() == null ? 0 : pedido.getValue();
            if (n <= 0) {
                continue;
            }
            Habitacion h = hotel.getHabitacion(pedido.getKey());
            if (h == null || calcularLibres(h, fechaEntrada, fechaSalida) < n) {
                throw new HabitacionNoDisponibleException(pedido.getKey());
            }
            reserva.addReservaTipoHabitacion(new ReservaTipoHabitacion(n, h));
        }
        if (reserva.getReservasPorTipo().isEmpty()) {
            throw new IllegalArgumentException("No se ha seleccionado ninguna habitación");
        }

        reserva.calcularImporte();
        reserva.setFechaBloqueo(LocalDateTime.now());
        reserva = reservasDAO.crearReserva(reserva);

        timerService.createSingleActionTimer(TIEMPO_BLOQUEO_MIN * 60_000L,
                new TimerConfig(reserva.getId(), false));
        return reserva;
    }

    @Override
    public Reserva confirmarReserva(Long idReserva, DatosCliente cliente, DatosPago pago)
            throws ReservaCaducadaException {
        if (idReserva == null || cliente == null || pago == null) {
            throw new IllegalArgumentException("Faltan los datos del cliente o del pago");
        }
        Reserva reserva = reservasDAO.reserva(idReserva);
        if (reserva == null || reserva.getEstado() != EstadoReserva.PENDIENTE
                || bloqueoExpirado(reserva)) {
            throw new ReservaCaducadaException();
        }
        reserva.setCliente(cliente);
        reserva.setDatosPago(pago);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        return reservasDAO.modificarReserva(reserva);
    }

    @Override
    public Reserva consultarReserva(Long idReserva, String dni) throws ReservaNoAccesibleException {
        if (idReserva == null) {
            return null;
        }
        Reserva reserva = reservasDAO.reserva(idReserva);
        if (reserva == null || reserva.getEstado() != EstadoReserva.CONFIRMADA) {
            return null;
        }
        if (dni == null || reserva.getCliente() == null
                || !dni.trim().equalsIgnoreCase(reserva.getCliente().getDni())) {
            throw new ReservaNoAccesibleException();
        }
        return reserva;
    }

    @Override
    public void cancelarReserva(Long idReserva, String dni)
            throws ReservaNoAccesibleException, CancelacionNoPermitidaException {
        Reserva reserva = consultarReserva(idReserva, dni);
        if (reserva == null) {
            throw new ReservaNoAccesibleException("No existe reserva");
        }
        long diasHastaEntrada = ChronoUnit.DAYS.between(LocalDate.now(), reserva.getFechaEntrada());
        if (diasHastaEntrada < LIMITE_CANCELACION_DIAS) {
            throw new CancelacionNoPermitidaException();
        }
        reservasDAO.eliminarReserva(reserva);
    }

    /**
     * Se ejecuta {@value #TIEMPO_BLOQUEO_MIN} minutos después de cada bloqueo: si la
     * reserva sigue pendiente, se elimina y las habitaciones vuelven a quedar libres.
     */
    @Timeout
    void liberarBloqueo(Timer timer) {
        Long idReserva = (Long) timer.getInfo();
        Reserva reserva = reservasDAO.reserva(idReserva);
        if (reserva != null && reserva.getEstado() == EstadoReserva.PENDIENTE) {
            reservasDAO.eliminarReserva(reserva);
        }
    }

    /** Habitaciones libres de un tipo: total menos las ocupadas en esas fechas. */
    private int calcularLibres(Habitacion habitacion, LocalDate entrada, LocalDate salida) {
        int ocupadas = reservasDAO.habitacionesReservadas(habitacion.getId(), entrada, salida);
        return Math.max(0, habitacion.getDisponibles() - ocupadas);
    }

    private static boolean fechasValidas(LocalDate entrada, LocalDate salida) {
        return entrada != null && salida != null && entrada.isBefore(salida);
    }

    /** Red de seguridad por si el temporizador se retrasa. */
    private static boolean bloqueoExpirado(Reserva reserva) {
        return reserva.getFechaBloqueo() != null
                && reserva.getFechaBloqueo().plusMinutes(TIEMPO_BLOQUEO_MIN).isBefore(LocalDateTime.now());
    }
}
