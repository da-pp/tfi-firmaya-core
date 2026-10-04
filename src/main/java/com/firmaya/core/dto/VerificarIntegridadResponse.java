package com.firmaya.core.dto;

/**
 * CU-13 pasos 9 a 12: resultado de la verificación con ambos hashes.
 */
public class VerificarIntegridadResponse {

    private boolean coincide;
    private String hashIngresado;
    private String hashAlmacenado;
    private Integer numeroVersion;
    private String mensaje;

    public boolean isCoincide() {
        return coincide;
    }

    public void setCoincide(boolean coincide) {
        this.coincide = coincide;
    }

    public String getHashIngresado() {
        return hashIngresado;
    }

    public void setHashIngresado(String hashIngresado) {
        this.hashIngresado = hashIngresado;
    }

    public String getHashAlmacenado() {
        return hashAlmacenado;
    }

    public void setHashAlmacenado(String hashAlmacenado) {
        this.hashAlmacenado = hashAlmacenado;
    }

    public Integer getNumeroVersion() {
        return numeroVersion;
    }

    public void setNumeroVersion(Integer numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
