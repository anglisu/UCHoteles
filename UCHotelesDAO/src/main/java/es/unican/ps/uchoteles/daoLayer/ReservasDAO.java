package es.unican.ps.uchoteles.daoLayer;

import java.time.LocalDate;
import java.util.List;

import es.unican.ps.uchoteles.common.daoLayer.IReservasDAO;
import es.unican.ps.uchoteles.common.domain.Reserva;
import es.unican.ps.uchoteles.common.exceptions.DataAccessException;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;

/**
 * Implementación JPA de IReservasDAO. Versión inicial para poder desplegar la
 * capa de negocio; se completará y probará en la práctica 3.3.
 */
@Stateless
public class ReservasDAO implements IReservasDAO {

    @PersistenceContext(unitName = "UCHotelesPU")
    private EntityManager em;

    @Override
    public Reserva crearReserva(Reserva reserva) {
        try {
            em.persist(reserva);
            em.flush();
            return reserva;
        } catch (PersistenceException e) {
            throw new DataAccessException("Error al crear la reserva", e);
        }
    }

    @Override
    public Reserva modificarReserva(Reserva reserva) {
        try {
            Reserva r = em.merge(reserva);
            em.flush();
            return r;
        } catch (PersistenceException e) {
            throw new DataAccessException("Error al modificar la reserva", e);
        }
    }

    @Override
    public void eliminarReserva(Reserva reserva) {
        try {
            em.remove(em.contains(reserva) ? reserva : em.merge(reserva));
        } catch (PersistenceException e) {
            throw new DataAccessException("Error al eliminar la reserva", e);
        }
    }

    @Override
    public Reserva reserva(Long id) {
        try {
            return em.find(Reserva.class, id);
        } catch (PersistenceException e) {
            throw new DataAccessException("Error al buscar la reserva", e);
        }
    }

    @Override
    public List<Reserva> reservasHotel(Long idHotel) {
        try {
            return em.createNamedQuery("Reserva.porHotel", Reserva.class)
                    .setParameter("idHotel", idHotel).getResultList();
        } catch (PersistenceException e) {
            throw new DataAccessException("Error al buscar las reservas del hotel", e);
        }
    }

    @Override
    public int habitacionesReservadas(Long idHabitacion, LocalDate fechaEntrada, LocalDate fechaSalida) {
        try {
            Number n = em.createNamedQuery("Reserva.habitacionesReservadas", Number.class)
                    .setParameter("idHabitacion", idHabitacion)
                    .setParameter("fechaEntrada", fechaEntrada)
                    .setParameter("fechaSalida", fechaSalida)
                    .getSingleResult();
            return n == null ? 0 : n.intValue();
        } catch (PersistenceException e) {
            throw new DataAccessException("Error al calcular las habitaciones reservadas", e);
        }
    }

    @Override
    public boolean existenReservasDesde(Long idHabitacion, LocalDate fecha) {
        try {
            Long n = em.createNamedQuery("Reserva.numReservasDesde", Long.class)
                    .setParameter("idHabitacion", idHabitacion)
                    .setParameter("fecha", fecha)
                    .getSingleResult();
            return n != null && n > 0;
        } catch (PersistenceException e) {
            throw new DataAccessException("Error al consultar las reservas", e);
        }
    }
}
