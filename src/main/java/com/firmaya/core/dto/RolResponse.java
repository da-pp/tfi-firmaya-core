package com.firmaya.core.dto;

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
