package es.unican.ps.uchoteles.common.daoLayer;

import java.time.LocalDate;
import java.util.List;

import es.unican.ps.uchoteles.common.domain.Reserva;
import jakarta.ejb.Local;

/**
 * CRUD de reservas y consultas de ocupación.
 * Todas las operaciones lanzan DataAccessException si falla el acceso a datos.
 */
@Local
public interface IReservasDAO {

    Reserva crearReserva(Reserva reserva);

    Reserva modificarReserva(Reserva reserva);

    void eliminarReserva(Reserva reserva);

    /** Reserva por clave primaria o null. */
    Reserva reserva(Long id);

    /** Reservas CONFIRMADAS del hotel ordenadas por fecha de entrada. */
    List<Reserva> reservasHotel(Long idHotel);

    /**
     * Suma de habitaciones de ese tipo reservadas (pendientes o confirmadas)
     * en reservas cuyas fechas se solapan con [fechaEntrada, fechaSalida).
     */
    int habitacionesReservadas(Long idHabitacion, LocalDate fechaEntrada, LocalDate fechaSalida);

    /** Indica si hay alguna reserva de ese tipo de habitación con salida posterior a la fecha. */
    boolean existenReservasDesde(Long idHabitacion, LocalDate fecha);
}
