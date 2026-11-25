package es.unican.ps.uchoteles.common.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * Reserva de una o varias habitaciones de un hotel para unas fechas. Mientras
 * está PENDIENTE no tiene datos de cliente ni de pago.
 */
@Entity
@Table(name = "Reservas")
@NamedQueries({
    @NamedQuery(name = "Reserva.porHotel",
                query = "SELECT r FROM Reserva r WHERE r.hotel.id = :idHotel "
                      + "AND r.estado = es.unican.ps.uchoteles.common.domain.EstadoReserva.CONFIRMADA "
                      + "ORDER BY r.fechaEntrada"),
    @NamedQuery(name = "Reserva.habitacionesReservadas",
                query = "SELECT COALESCE(SUM(rt.numHabitaciones), 0) FROM ReservaTipoHabitacion rt "
                      + "WHERE rt.habitacion.id = :idHabitacion "
                      + "AND rt.reserva.fechaEntrada < :fechaSalida "
                      + "AND rt.reserva.fechaSalida > :fechaEntrada"),
    @NamedQuery(name = "Reserva.numReservasDesde",
                query = "SELECT COUNT(rt) FROM ReservaTipoHabitacion rt "
                      + "WHERE rt.habitacion.id = :idHabitacion "
                      + "AND rt.reserva.fechaSalida > :fecha")
})
public class Reserva implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fechaEntrada;

    private LocalDate fechaSalida;

    private double importe;

    @Enumerated(EnumType.STRING)
    private EstadoReserva estado;

    private LocalDateTime fechaBloqueo;

    @ManyToOne
    @JoinColumn(name = "hotel_fk")
    private Hotel hotel;

    @OneToMany(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true,
               fetch = FetchType.EAGER)
    private List<ReservaTipoHabitacion> reservasPorTipo = new ArrayList<>();

    @Embedded
    private DatosCliente cliente;

    @Embedded
    private DatosPago datosPago;

    public Reserva() {
    }

    public Reserva(Hotel hotel, LocalDate fechaEntrada, LocalDate fechaSalida, EstadoReserva estado) {
        this.hotel = hotel;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.estado = estado;
    }

    /** Número de noches entre la fecha de entrada y la de salida. */
    public long getNumNoches() {
        if (fechaEntrada == null || fechaSalida == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
    }

    /**
     * Calcula el importe total (habitaciones x precio por noche x noches),
     * lo guarda en {@code importe} y lo devuelve.
     */
    public double calcularImporte() {
        double porNoche = 0;
        for (ReservaTipoHabitacion rt : reservasPorTipo) {
            porNoche += rt.getNumHabitaciones() * rt.getHabitacion().getPrecioPorNoche();
        }
        importe = porNoche * getNumNoches();
        return importe;
    }

    /** Añade una línea de reserva manteniendo la asociación bidireccional. */
    public void addReservaTipoHabitacion(ReservaTipoHabitacion rt) {
        reservasPorTipo.add(rt);
        rt.setReserva(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDate fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public LocalDate getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDate fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public double getImporte() {
        return importe;
    }

    public void setImporte(double importe) {
        this.importe = importe;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaBloqueo() {
        return fechaBloqueo;
    }

    public void setFechaBloqueo(LocalDateTime fechaBloqueo) {
        this.fechaBloqueo = fechaBloqueo;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public List<ReservaTipoHabitacion> getReservasPorTipo() {
        return reservasPorTipo;
    }

    public void setReservasPorTipo(List<ReservaTipoHabitacion> reservasPorTipo) {
        this.reservasPorTipo = reservasPorTipo;
    }

    public DatosCliente getCliente() {
        return cliente;
    }

    public void setCliente(DatosCliente cliente) {
        this.cliente = cliente;
    }

    public DatosPago getDatosPago() {
        return datosPago;
    }

    public void setDatosPago(DatosPago datosPago) {
        this.datosPago = datosPago;
    }

    @Override
    public String toString() {
        return "Reserva[id=" + id + ", entrada=" + fechaEntrada + ", salida=" + fechaSalida
                + ", importe=" + importe + ", estado=" + estado + "]";
    }
}
