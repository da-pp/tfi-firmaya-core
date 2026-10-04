package com.firmaya.core.dto;

import java.time.LocalDateTime;

/**
 * Firmante del contrato y estado de su firma (CU-07 pasos 3 a 6, CU-09 pasos 3 a 10).
 */
public class FirmanteResponse {

    private Integer idParte;
    private Integer idFirma;
    private String nombre;
    private String email;
    private String estadoFirma;
    private LocalDateTime fechaEvento;
    // Solo para firmas completadas (CU-09 paso 9)
    private String hashFirma;
    private String direccionIp;
    // Enlace de firma para "Copiar enlace" (solo si hay solicitud y no está firmada)
    private String enlace;
    // Resultado del último envío (solo al solicitar, reintentar o reenviar)
    private Boolean envioExitoso;

    public Integer getIdParte() {
        return idParte;
    }

    public void setIdParte(Integer idParte) {
        this.idParte = idParte;
    }

    public Integer getIdFirma() {
        return idFirma;
    }

    public void setIdFirma(Integer idFirma) {
        this.idFirma = idFirma;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEstadoFirma() {
        return estadoFirma;
    }

    public void setEstadoFirma(String estadoFirma) {
        this.estadoFirma = estadoFirma;
    }

    public LocalDateTime getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(LocalDateTime fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    public String getHashFirma() {
        return hashFirma;
    }

    public void setHashFirma(String hashFirma) {
        this.hashFirma = hashFirma;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }

    public String getEnlace() {
        return enlace;
    }

    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }

    public Boolean getEnvioExitoso() {
        return envioExitoso;
    }

    public void setEnvioExitoso(Boolean envioExitoso) {
        this.envioExitoso = envioExitoso;
    }
}
