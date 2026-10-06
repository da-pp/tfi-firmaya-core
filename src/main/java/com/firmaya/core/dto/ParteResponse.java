package com.firmaya.core.dto;

public class ParteResponse {

    private Integer idParte;
    private String nombre;
    private String email;
    private String rol;
    private String estadoInvitacion;
    private String enlace;

    public Integer getIdParte() {
        return idParte;
    }

    public void setIdParte(Integer idParte) {
        this.idParte = idParte;
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

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getEstadoInvitacion() {
        return estadoInvitacion;
    }

    public void setEstadoInvitacion(String estadoInvitacion) {
        this.estadoInvitacion = estadoInvitacion;
    }

    public String getEnlace() {
        return enlace;
    }

    public void setEnlace(String enlace) {
        this.enlace = enlace;
    }
}
