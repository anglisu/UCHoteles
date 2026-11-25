package es.unican.ps.uchoteles.common.businessLayer;

import java.util.List;

import es.unican.ps.uchoteles.common.domain.Habitacion;
import es.unican.ps.uchoteles.common.domain.Hotel;
import es.unican.ps.uchoteles.common.domain.Reserva;
import es.unican.ps.uchoteles.common.domain.TipoHabitacion;
import es.unican.ps.uchoteles.common.exceptions.OperacionNoValidaException;
import jakarta.ejb.Local;

/**
 * Operaciones del gerente (se protegerán con el rol GERENTE en la práctica 3.5).
 * Todas las operaciones pueden lanzar DataAccessException.
 */
@Local
public interface IGestionHoteles {

    /** Hotel por nombre (null si no existe). */
    Hotel consultarHotel(String nombre);

    /** Reservas confirmadas del hotel ordenadas por fecha de entrada. */
    List<Reserva> consultarReservasHotel(Long idHotel);

    /**
     * Actualiza el precio por noche y el número de habitaciones de los tipos indicados.
     *
     * @throws OperacionNoValidaException si el hotel no existe, algún tipo no está en
     *         el hotel o algún valor no es válido
     */
    Hotel modificarHabitaciones(Long idHotel, List<Habitacion> habitaciones)
            throws OperacionNoValidaException;

    /**
     * Añade un tipo de habitación nuevo (no repetido) al hotel.
     *
     * @throws OperacionNoValidaException si el hotel no existe, el tipo ya existe
     *         o los datos no son válidos
     */
    Hotel anhadirTipoHabitacion(Long idHotel, Habitacion habitacion) throws OperacionNoValidaException;

    /**
     * Elimina un tipo de habitación si no tiene reservas futuras.
     *
     * @throws OperacionNoValidaException si el hotel o el tipo no existen, hay reservas
     *         futuras de ese tipo o es el único tipo del hotel
     */
    Hotel eliminarTipoHabitacion(Long idHotel, TipoHabitacion tipo) throws OperacionNoValidaException;
}
