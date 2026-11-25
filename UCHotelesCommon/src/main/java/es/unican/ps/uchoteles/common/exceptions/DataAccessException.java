package es.unican.ps.uchoteles.common.exceptions;

import jakarta.ejb.ApplicationException;

/**
 * Fallo en el acceso a la base de datos. No comprobada y con rollback de la
 * transacción; la capa web la traduce a "Error en acceso a datos".
 */
@ApplicationException(rollback = true)
public class DataAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataAccessException(String mensaje) {
        super(mensaje);
    }

    public DataAccessException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
