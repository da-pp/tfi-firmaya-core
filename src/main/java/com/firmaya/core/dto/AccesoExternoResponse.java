package com.firmaya.core.dto;

import java.time.LocalDateTime;

/**
 * CU-04 pasos 5 a 16: contrato visto por una parte invitada.
 */
public class AccesoExternoResponse {

    private Integer idContrato;
    private String nombreContrato;
    private String estado;
    private Integer numeroVersion;
    private LocalDateTime fechaUltimaModificacion;
    private String hash;
    private String contenido;
    private String nombreParte;
    private String rolParte;
    // Paso 15: rol Firmante y contrato "Listo para firmar"
    private boolean puedeFirmar;
    private LocalDateTime fechaExpiracionToken;

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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getNumeroVersion() {
        return numeroVersion;
    }

    public void setNumeroVersion(Integer numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public LocalDateTime getFechaUltimaModificacion() {
        return fechaUltimaModificacion;
    }

    public void setFechaUltimaModificacion(LocalDateTime fechaUltimaModificacion) {
        this.fechaUltimaModificacion = fechaUltimaModificacion;
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

    public String getNombreParte() {
        return nombreParte;
    }

    public void setNombreParte(String nombreParte) {
        this.nombreParte = nombreParte;
    }

    public String getRolParte() {
        return rolParte;
    }

    public void setRolParte(String rolParte) {
        this.rolParte = rolParte;
    }

    public boolean isPuedeFirmar() {
        return puedeFirmar;
    }

    public void setPuedeFirmar(boolean puedeFirmar) {
        this.puedeFirmar = puedeFirmar;
    }

    public LocalDateTime getFechaExpiracionToken() {
        return fechaExpiracionToken;
    }

    public void setFechaExpiracionToken(LocalDateTime fechaExpiracionToken) {
        this.fechaExpiracionToken = fechaExpiracionToken;
    }
}
