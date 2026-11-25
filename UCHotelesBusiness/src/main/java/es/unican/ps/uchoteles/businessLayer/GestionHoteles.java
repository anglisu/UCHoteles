package es.unican.ps.uchoteles.businessLayer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import es.unican.ps.uchoteles.common.businessLayer.IConsultaHoteles;
import es.unican.ps.uchoteles.common.businessLayer.IGestionHoteles;
import es.unican.ps.uchoteles.common.daoLayer.IHotelesDAO;
import es.unican.ps.uchoteles.common.daoLayer.IReservasDAO;
import es.unican.ps.uchoteles.common.domain.Habitacion;
import es.unican.ps.uchoteles.common.domain.Hotel;
import es.unican.ps.uchoteles.common.domain.Reserva;
import es.unican.ps.uchoteles.common.domain.TipoHabitacion;
import es.unican.ps.uchoteles.common.exceptions.OperacionNoValidaException;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

/**
 * Búsqueda de hoteles (usuario) y gestión de hoteles y sus tipos de habitación
 * (gerente). En la práctica 3.5 los métodos de IGestionHoteles se anotarán con
 * {@code @RolesAllowed("GERENTE")}.
 */
@Stateless
public class GestionHoteles implements IConsultaHoteles, IGestionHoteles {

    @EJB
    private IHotelesDAO hotelesDAO;

    @EJB
    private IReservasDAO reservasDAO;

    // ---------------------------------------------------------------- IConsultaHoteles

    @Override
    public Hotel buscarHotelPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        return hotelesDAO.hotelPorNombre(nombre.trim());
    }

    @Override
    public List<Hotel> buscarHotelesPorLocalidad(String localidad) {
        if (localidad == null || localidad.isBlank()) {
            return new ArrayList<>();
        }
        return hotelesDAO.hotelesPorLocalidad(localidad.trim());
    }

    // ---------------------------------------------------------------- IGestionHoteles

    @Override
    public Hotel consultarHotel(String nombre) {
        return buscarHotelPorNombre(nombre);
    }

    @Override
    public List<Reserva> consultarReservasHotel(Long idHotel) {
        if (idHotel == null) {
            return new ArrayList<>();
        }
        return reservasDAO.reservasHotel(idHotel);
    }

    @Override
    public Hotel modificarHabitaciones(Long idHotel, List<Habitacion> habitaciones)
            throws OperacionNoValidaException {
        Hotel hotel = buscarHotel(idHotel);
        if (habitaciones == null || habitaciones.isEmpty()) {
            throw new OperacionNoValidaException("No hay habitaciones que modificar");
        }
        // Primero se valida todo, para no dejar el hotel modificado a medias
        for (Habitacion h : habitaciones) {
            validar(h);
            if (hotel.getHabitacion(h.getTipo()) == null) {
                throw new OperacionNoValidaException("El hotel no tiene habitaciones de tipo " + h.getTipo());
            }
        }
        for (Habitacion h : habitaciones) {
            Habitacion actual = hotel.getHabitacion(h.getTipo());
            actual.setPrecioPorNoche(h.getPrecioPorNoche());
            actual.setDisponibles(h.getDisponibles());
        }
        return hotelesDAO.modificarHotel(hotel);
    }

    @Override
    public Hotel anhadirTipoHabitacion(Long idHotel, Habitacion habitacion) throws OperacionNoValidaException {
        Hotel hotel = buscarHotel(idHotel);
        validar(habitacion);
        if (hotel.getHabitacion(habitacion.getTipo()) != null) {
            throw new OperacionNoValidaException("El hotel ya tiene habitaciones de tipo " + habitacion.getTipo());
        }
        hotel.addHabitacion(habitacion);
        return hotelesDAO.modificarHotel(hotel);
    }

    @Override
    public Hotel eliminarTipoHabitacion(Long idHotel, TipoHabitacion tipo) throws OperacionNoValidaException {
        Hotel hotel = buscarHotel(idHotel);
        Habitacion h = hotel.getHabitacion(tipo);
        if (h == null) {
            throw new OperacionNoValidaException("El hotel no tiene habitaciones de tipo " + tipo);
        }
        if (hotel.getHabitaciones().size() == 1) {
            throw new OperacionNoValidaException("Un hotel debe tener al menos un tipo de habitación");
        }
        if (reservasDAO.existenReservasDesde(h.getId(), LocalDate.now())) {
            throw new OperacionNoValidaException("Existen reservas futuras de habitaciones de tipo " + tipo);
        }
        hotel.removeHabitacion(h);
        return hotelesDAO.modificarHotel(hotel);
    }

    // ---------------------------------------------------------------- auxiliares

    private Hotel buscarHotel(Long idHotel) throws OperacionNoValidaException {
        Hotel hotel = idHotel == null ? null : hotelesDAO.hotel(idHotel);
        if (hotel == null) {
            throw new OperacionNoValidaException("Hotel no encontrado");
        }
        return hotel;
    }

    /** Una habitación es válida si tiene tipo, precio positivo y número de habitaciones no negativo. */
    private void validar(Habitacion habitacion) throws OperacionNoValidaException {
        if (habitacion == null || habitacion.getTipo() == null) {
            throw new OperacionNoValidaException("Falta el tipo de habitación");
        }
        if (habitacion.getPrecioPorNoche() <= 0) {
            throw new OperacionNoValidaException("El precio por noche debe ser mayor que 0");
        }
        if (habitacion.getDisponibles() < 0) {
            throw new OperacionNoValidaException("El número de habitaciones no puede ser negativo");
        }
    }
}
