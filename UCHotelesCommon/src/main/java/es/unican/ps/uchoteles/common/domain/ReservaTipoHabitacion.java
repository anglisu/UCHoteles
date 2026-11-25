package es.unican.ps.uchoteles.common.domain;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Línea de una reserva: cuántas habitaciones de un tipo concreto se reservan.
 * Es la clase asociación del modelo de dominio convertida en entidad.
 */
@Entity
@Table(name = "ReservasTipoHabitacion")
public class ReservaTipoHabitacion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numHabitaciones;

    @ManyToOne
    @JoinColumn(name = "reserva_fk")
    private Reserva reserva;

    @ManyToOne
    @JoinColumn(name = "habitacion_fk")
    private Habitacion habitacion;

    public ReservaTipoHabitacion() {
    }

    public ReservaTipoHabitacion(int numHabitaciones, Habitacion habitacion) {
        this.numHabitaciones = numHabitaciones;
        this.habitacion = habitacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumHabitaciones() {
        return numHabitaciones;
    }

    public void setNumHabitaciones(int numHabitaciones) {
        this.numHabitaciones = numHabitaciones;
    }

    public Reserva getReserva() {
        return reserva;
    }

    public void setReserva(Reserva reserva) {
        this.reserva = reserva;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
    }
}
