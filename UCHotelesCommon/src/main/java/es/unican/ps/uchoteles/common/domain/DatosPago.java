package es.unican.ps.uchoteles.common.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/** Datos de la tarjeta con la que se paga la reserva. */
@Embeddable
public class DatosPago implements Serializable {

    private static final long serialVersionUID = 1L;

    private String numTarjeta;
    private String cvc;
    private int mesCaducidad;
    private int anhoCaducidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipoTarjeta")
    private TipoTarjeta tipo;

    public DatosPago() {
    }

    public DatosPago(String numTarjeta, String cvc, int mesCaducidad, int anhoCaducidad,
                     TipoTarjeta tipo) {
        this.numTarjeta = numTarjeta;
        this.cvc = cvc;
        this.mesCaducidad = mesCaducidad;
        this.anhoCaducidad = anhoCaducidad;
        this.tipo = tipo;
    }

    public String getNumTarjeta() {
        return numTarjeta;
    }

    public void setNumTarjeta(String numTarjeta) {
        this.numTarjeta = numTarjeta;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public int getMesCaducidad() {
        return mesCaducidad;
    }

    public void setMesCaducidad(int mesCaducidad) {
        this.mesCaducidad = mesCaducidad;
    }

    public int getAnhoCaducidad() {
        return anhoCaducidad;
    }

    public void setAnhoCaducidad(int anhoCaducidad) {
        this.anhoCaducidad = anhoCaducidad;
    }

    public TipoTarjeta getTipo() {
        return tipo;
    }

    public void setTipo(TipoTarjeta tipo) {
        this.tipo = tipo;
    }
}
