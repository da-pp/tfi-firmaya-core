package com.firmaya.core.dto;

import java.util.List;

/**
 * CU-12 pasos 9 a 15: comparación entre la Versión A (base) y la Versión B.
 */
public class CompararVersionesResponse {

    private VersionResponse versionA;
    private VersionResponse versionB;
    private List<FragmentoComparacion> fragmentos;
    private int cantidadCambios;
    private String mensaje;

    public VersionResponse getVersionA() {
        return versionA;
    }

    public void setVersionA(VersionResponse versionA) {
        this.versionA = versionA;
    }

    public VersionResponse getVersionB() {
        return versionB;
    }

    public void setVersionB(VersionResponse versionB) {
        this.versionB = versionB;
    }

    public List<FragmentoComparacion> getFragmentos() {
        return fragmentos;
    }

    public void setFragmentos(List<FragmentoComparacion> fragmentos) {
        this.fragmentos = fragmentos;
    }

    public int getCantidadCambios() {
        return cantidadCambios;
    }

    public void setCantidadCambios(int cantidadCambios) {
        this.cantidadCambios = cantidadCambios;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
