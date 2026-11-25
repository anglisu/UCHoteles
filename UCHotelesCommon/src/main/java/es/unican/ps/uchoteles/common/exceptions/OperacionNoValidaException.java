package es.unican.ps.uchoteles.common.exceptions;

/** El gerente intenta una modificación no válida sobre las habitaciones de un hotel. */
public class OperacionNoValidaException extends Exception {

    private static final long serialVersionUID = 1L;

    public OperacionNoValidaException() {
        super("Operación no válida");
    }

    public OperacionNoValidaException(String mensaje) {
        super(mensaje);
    }
}
