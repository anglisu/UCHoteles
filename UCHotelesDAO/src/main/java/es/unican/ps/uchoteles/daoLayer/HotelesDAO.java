package es.unican.ps.uchoteles.daoLayer;

import java.util.List;

import es.unican.ps.uchoteles.common.daoLayer.IHotelesDAO;
import es.unican.ps.uchoteles.common.domain.Hotel;
import es.unican.ps.uchoteles.common.exceptions.DataAccessException;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;

/**
 * Implementación JPA de IHotelesDAO. Versión inicial para poder desplegar la
 * capa de negocio; se completará y probará en la práctica 3.3.
 */
@Stateless
public class HotelesDAO implements IHotelesDAO {

    @PersistenceContext(unitName = "UCHotelesPU")
    private EntityManager em;

    @Override
    public Hotel crearHotel(Hotel hotel) {
        try {
            em.persist(hotel);
            em.flush();
            return hotel;
        } catch (PersistenceException e) {
            throw new DataAccessException("Error al crear el hotel", e);
        }
    }

    @Override
    public Hotel modificarHotel(Hotel hotel) {
        try {
            Hotel h = em.merge(hotel);
            em.flush();
            return h;
        } catch (PersistenceException e) {
            throw new DataAccessException("Error al modificar el hotel", e);
        }
    }

    @Override
    public void eliminarHotel(Hotel hotel) {
        try {
            em.remove(em.contains(hotel) ? hotel : em.merge(hotel));
        } catch (PersistenceException e) {
            throw new DataAccessException("Error al eliminar el hotel", e);
        }
    }

    @Override
    public Hotel hotel(Long id) {
        try {
            return em.find(Hotel.class, id);
        } catch (PersistenceException e) {
            throw new DataAccessException("Error al buscar el hotel", e);
        }
    }

    @Override
    public Hotel hotelPorNombre(String nombre) {
        try {
            List<Hotel> res = em.createNamedQuery("Hotel.porNombre", Hotel.class)
                    .setParameter("nombre", nombre).getResultList();
            return res.isEmpty() ? null : res.get(0);
        } catch (PersistenceException e) {
            throw new DataAccessException("Error al buscar el hotel", e);
        }
    }

    @Override
    public List<Hotel> hotelesPorLocalidad(String localidad) {
        try {
            return em.createNamedQuery("Hotel.porLocalidad", Hotel.class)
                    .setParameter("localidad", localidad).getResultList();
        } catch (PersistenceException e) {
            throw new DataAccessException("Error al buscar hoteles", e);
        }
    }

    @Override
    public List<Hotel> hoteles() {
        try {
            return em.createNamedQuery("Hotel.todos", Hotel.class).getResultList();
        } catch (PersistenceException e) {
            throw new DataAccessException("Error al buscar hoteles", e);
        }
    }
}
