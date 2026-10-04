package com.firmaya.core.dto;

/**
 * Opción del desplegable de rol (CU-15 paso 8).
 */
public class RolResponse {

    private Integer idRol;
    private String nombre;

    public RolResponse(Integer idRol, String nombre) {
        this.idRol = idRol;
        this.nombre = nombre;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public String getNombre() {
        return nombre;
    }
}
