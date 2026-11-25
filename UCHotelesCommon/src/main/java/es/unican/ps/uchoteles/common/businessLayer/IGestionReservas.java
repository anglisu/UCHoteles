package es.unican.ps.uchoteles.common.businessLayer;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import es.unican.ps.uchoteles.common.domain.DatosCliente;
import es.unican.ps.uchoteles.common.domain.DatosPago;
import es.unican.ps.uchoteles.common.domain.DisponibilidadHabitacion;
import es.unican.ps.uchoteles.common.domain.Reserva;
import es.unican.ps.uchoteles.common.domain.TipoHabitacion;
import es.unican.ps.uchoteles.common.exceptions.CancelacionNoPermitidaException;
import es.unican.ps.uchoteles.common.exceptions.HabitacionNoDisponibleException;
import es.unican.ps.uchoteles.common.exceptions.ReservaCaducadaException;
import es.unican.ps.uchoteles.common.exceptions.ReservaNoAccesibleException;
import jakarta.ejb.Local;

/**
 * Realizar, consultar y cancelar reservas (actor Usuario).
 * Todas las operaciones pueden lanzar DataAccessException.
 */
@Local
public interface IGestionReservas {

    /**
     * Por cada tipo de habitación del hotel, habitaciones libres entre las fechas
     * (total menos las ocupadas por reservas confirmadas o bloqueadas) y precio
     * por noche. Lista vacía si el hotel no existe o las fechas no son válidas.
     */
    List<DisponibilidadHabitacion> consultarDisponibilidad(Long idHotel, LocalDate fechaEntrada,
                                                           LocalDate fechaSalida);

    /**
     * Comprueba la disponibilidad de cada tipo pedido, crea la reserva en estado
     * PENDIENTE con su importe y programa su liberación a los 10 minutos.
     *
     * @throws HabitacionNoDisponibleException si no hay suficientes habitaciones libres de algún tipo
     */
    Reserva bloquearHabitaciones(Long idHotel, LocalDate fechaEntrada, LocalDate fechaSalida,
                                 Map<TipoHabitacion, Integer> habitaciones)
            throws HabitacionNoDisponibleException;

    /**
     * Añade los datos de cliente y pago a la reserva bloqueada y la pasa a
     * CONFIRMADA. Devuelve la reserva con su identificador.
     *
     * @throws ReservaCaducadaException si el bloqueo de 10 minutos ya ha expirado
     */
    Reserva confirmarReserva(Long idReserva, DatosCliente cliente, DatosPago pago)
            throws ReservaCaducadaException;

    /**
     * Devuelve la reserva confirmada con ese id o null si no existe.
     *
     * @throws ReservaNoAccesibleException si la reserva no corresponde al DNI
     */
    Reserva consultarReserva(Long idReserva, String dni) throws ReservaNoAccesibleException;

    /**
     * Elimina la reserva si quedan al menos 2 días para la fecha de entrada.
     *
     * @throws ReservaNoAccesibleException si la reserva no existe o no corresponde al DNI
     * @throws CancelacionNoPermitidaException si quedan menos de 2 días para la entrada
     */
    void cancelarReserva(Long idReserva, String dni)
            throws ReservaNoAccesibleException, CancelacionNoPermitidaException;
}
