package es.unican.ps.uchoteles.common.domain;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Tipo de habitación ofrecido por un hotel. {@code disponibles} es el número
 * total de habitaciones de ese tipo; las libres para unas fechas concretas se
 * calculan restando las reservadas.
 */
@Entity
@Table(name = "Habitaciones")
public class Habitacion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoHabitacion tipo;

    private double precioPorNoche;

    private int disponibles;

    @ManyToOne
    @JoinColumn(name = "hotel_fk")
    private Hotel hotel;

    public Habitacion() {
    }

    public Habitacion(TipoHabitacion tipo, double precioPorNoche, int disponibles) {
        this.tipo = tipo;
        this.precioPorNoche = precioPorNoche;
        this.disponibles = disponibles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoHabitacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoHabitacion tipo) {
        this.tipo = tipo;
    }

    public double getPrecioPorNoche() {
        return precioPorNoche;
    }

    public void setPrecioPorNoche(double precioPorNoche) {
        this.precioPorNoche = precioPorNoche;
    }

    public int getDisponibles() {
        return disponibles;
    }

    public void setDisponibles(int disponibles) {
        this.disponibles = disponibles;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    @Override
    public String toString() {
        return "Habitacion[id=" + id + ", tipo=" + tipo + ", precioPorNoche=" + precioPorNoche
                + ", disponibles=" + disponibles + "]";
    }
}
