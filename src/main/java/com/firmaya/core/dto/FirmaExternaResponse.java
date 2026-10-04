package com.firmaya.core.dto;

/**
 * CU-08 pasos 3 a 7: datos del contrato en la pantalla de firma.
 */
public class FirmaExternaResponse {

    private Integer idContrato;
    private String nombreContrato;
    private Integer numeroVersion;
    private String hash;
    private String contenido;
    private String nombreFirmante;
    private String correoFirmante;

    public Integer getIdContrato() {
        return idContrato;
    }

    public void setIdContrato(Integer idContrato) {
        this.idContrato = idContrato;
    }

    public String getNombreContrato() {
        return nombreContrato;
    }

    public void setNombreContrato(String nombreContrato) {
        this.nombreContrato = nombreContrato;
    }

    public Integer getNumeroVersion() {
        return numeroVersion;
    }

    public void setNumeroVersion(Integer numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getNombreFirmante() {
        return nombreFirmante;
    }

    public void setNombreFirmante(String nombreFirmante) {
        this.nombreFirmante = nombreFirmante;
    }

    public String getCorreoFirmante() {
        return correoFirmante;
    }

    public void setCorreoFirmante(String correoFirmante) {
        this.correoFirmante = correoFirmante;
    }
}
