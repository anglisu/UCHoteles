package es.unican.ps.uchoteles.common.exceptions;

/** Se intenta confirmar una reserva cuyo bloqueo de 10 minutos ya ha expirado. */
public class ReservaCaducadaException extends Exception {

    private static final long serialVersionUID = 1L;

    public ReservaCaducadaException() {
        super("El tiempo de bloqueo ha expirado");
    }

    public ReservaCaducadaException(String mensaje) {
        super(mensaje);
    }
}
