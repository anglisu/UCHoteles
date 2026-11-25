package es.unican.ps.uchoteles.common.exceptions;

/** La reserva existe pero no corresponde al DNI indicado. */
public class ReservaNoAccesibleException extends Exception {

    private static final long serialVersionUID = 1L;

    public ReservaNoAccesibleException() {
        super("Reserva no accesible");
    }

    public ReservaNoAccesibleException(String mensaje) {
        super(mensaje);
    }
}
