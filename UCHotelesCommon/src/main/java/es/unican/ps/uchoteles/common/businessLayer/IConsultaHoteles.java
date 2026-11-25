package es.unican.ps.uchoteles.common.businessLayer;

import java.util.List;

import es.unican.ps.uchoteles.common.domain.Hotel;
import jakarta.ejb.Local;

/**
 * Búsqueda de hoteles por parte de cualquier usuario.
 * Todas las operaciones pueden lanzar DataAccessException.
 */
@Local
public interface IConsultaHoteles {

    /** Devuelve el hotel con ese nombre o null si no existe. */
    Hotel buscarHotelPorNombre(String nombre);

    /** Hoteles de la localidad (lista vacía si no hay ninguno). */
    List<Hotel> buscarHotelesPorLocalidad(String localidad);
}
