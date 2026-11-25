package es.unican.ps.uchoteles.common.exceptions;

/** Quedan menos de 2 días para la fecha de entrada de la reserva. */
public class CancelacionNoPermitidaException extends Exception {

    private static final long serialVersionUID = 1L;

    public CancelacionNoPermitidaException() {
        super("Límite de cancelación de reserva sobrepasado");
    }

    public CancelacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}
