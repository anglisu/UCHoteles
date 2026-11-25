package es.unican.ps.uchoteles.common.domain;

import java.io.Serializable;

/**
 * DTO (no persistente) con la disponibilidad de un tipo de habitación de un
 * hotel para unas fechas concretas.
 */
public class DisponibilidadHabitacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private TipoHabitacion tipo;
    private int libres;
    private double precioPorNoche;

    public DisponibilidadHabitacion() {
    }

    public DisponibilidadHabitacion(TipoHabitacion tipo, int libres, double precioPorNoche) {
        this.tipo = tipo;
        this.libres = libres;
        this.precioPorNoche = precioPorNoche;
    }

    public TipoHabitacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoHabitacion tipo) {
        this.tipo = tipo;
    }

    public int getLibres() {
        return libres;
    }

    public void setLibres(int libres) {
        this.libres = libres;
    }

    public double getPrecioPorNoche() {
        return precioPorNoche;
    }

    public void setPrecioPorNoche(double precioPorNoche) {
        this.precioPorNoche = precioPorNoche;
    }

    @Override
    public String toString() {
        return "DisponibilidadHabitacion[tipo=" + tipo + ", libres=" + libres
                + ", precioPorNoche=" + precioPorNoche + "]";
    }
}
