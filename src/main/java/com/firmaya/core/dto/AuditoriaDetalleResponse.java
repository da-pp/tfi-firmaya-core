package com.firmaya.core.dto;

/**
 * CU-18 paso 18: detalle expandido de un registro.
 */
public class AuditoriaDetalleResponse {

    private AuditoriaResponse registro;
    private String datosAntes;
    private String datosDespues;
    private Integer numeroVersion;
    private String hashVersion;

    public AuditoriaResponse getRegistro() {
        return registro;
    }

    public void setRegistro(AuditoriaResponse registro) {
        this.registro = registro;
    }

    public String getDatosAntes() {
        return datosAntes;
    }

    public void setDatosAntes(String datosAntes) {
        this.datosAntes = datosAntes;
    }

    public String getDatosDespues() {
        return datosDespues;
    }

    public void setDatosDespues(String datosDespues) {
        this.datosDespues = datosDespues;
    }

    public Integer getNumeroVersion() {
        return numeroVersion;
    }

    public void setNumeroVersion(Integer numeroVersion) {
        this.numeroVersion = numeroVersion;
    }

    public String getHashVersion() {
        return hashVersion;
    }

    public void setHashVersion(String hashVersion) {
        this.hashVersion = hashVersion;
    }
}
