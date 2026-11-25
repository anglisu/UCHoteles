package es.unican.ps.uchoteles.common.exceptions;

import es.unican.ps.uchoteles.common.domain.TipoHabitacion;

/** No quedan habitaciones suficientes de un tipo para las fechas pedidas. */
public class HabitacionNoDisponibleException extends Exception {

    private static final long serialVersionUID = 1L;

    private final TipoHabitacion tipo;

    public HabitacionNoDisponibleException(TipoHabitacion tipo) {
        super("Habitación de tipo " + tipo + " no disponible");
        this.tipo = tipo;
    }

    public TipoHabitacion getTipo() {
        return tipo;
    }
}
