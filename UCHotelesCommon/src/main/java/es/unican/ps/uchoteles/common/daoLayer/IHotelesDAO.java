package es.unican.ps.uchoteles.common.daoLayer;

import java.util.List;

import es.unican.ps.uchoteles.common.domain.Hotel;
import jakarta.ejb.Local;

/**
 * CRUD de hoteles. Las habitaciones se gestionan en cascada desde Hotel.
 * Todas las operaciones lanzan DataAccessException si falla el acceso a datos.
 */
@Local
public interface IHotelesDAO {

    Hotel crearHotel(Hotel hotel);

    Hotel modificarHotel(Hotel hotel);

    void eliminarHotel(Hotel hotel);

    /** Hotel por clave primaria o null. */
    Hotel hotel(Long id);

    /** Hotel con ese nombre o null. */
    Hotel hotelPorNombre(String nombre);

    List<Hotel> hotelesPorLocalidad(String localidad);

    List<Hotel> hoteles();
}
