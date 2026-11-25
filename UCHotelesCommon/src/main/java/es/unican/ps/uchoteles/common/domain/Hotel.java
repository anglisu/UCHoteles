package es.unican.ps.uchoteles.common.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Hotel de la cadena. Compone sus habitaciones (una por tipo), que se
 * persisten y eliminan en cascada a través del propio hotel.
 */
@Entity
@Table(name = "Hoteles")
@NamedQueries({
    @NamedQuery(name = "Hotel.porNombre",
                query = "SELECT h FROM Hotel h WHERE h.nombre = :nombre"),
    @NamedQuery(name = "Hotel.porLocalidad",
                query = "SELECT h FROM Hotel h WHERE h.localidad = :localidad ORDER BY h.nombre"),
    @NamedQuery(name = "Hotel.todos",
                query = "SELECT h FROM Hotel h ORDER BY h.nombre")
})
public class Hotel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    private String direccion;

    private String localidad;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true,
               fetch = FetchType.EAGER)
    private List<Habitacion> habitaciones = new ArrayList<>();

    @OneToMany(mappedBy = "hotel")
    private List<Reserva> reservas = new ArrayList<>();

    public Hotel() {
    }

    public Hotel(String nombre, String direccion, String localidad, Categoria categoria) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.localidad = localidad;
        this.categoria = categoria;
    }

    /**
     * Devuelve la habitación del tipo indicado o null si el hotel no ofrece ese tipo.
     */
    public Habitacion getHabitacion(TipoHabitacion tipo) {
        for (Habitacion h : habitaciones) {
            if (h.getTipo() == tipo) {
                return h;
            }
        }
        return null;
    }

    /** Añade una habitación al hotel manteniendo la asociación bidireccional. */
    public void addHabitacion(Habitacion habitacion) {
        habitaciones.add(habitacion);
        habitacion.setHotel(this);
    }

    /** Quita una habitación del hotel (con orphanRemoval se borra de la BD). */
    public void removeHabitacion(Habitacion habitacion) {
        habitaciones.remove(habitacion);
        habitacion.setHotel(null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public List<Habitacion> getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(List<Habitacion> habitaciones) {
        this.habitaciones = habitaciones;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    @Override
    public String toString() {
        return "Hotel[id=" + id + ", nombre=" + nombre + ", localidad=" + localidad
                + ", categoria=" + categoria + "]";
    }
}
