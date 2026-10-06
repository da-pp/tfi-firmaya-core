package com.firmaya.core.dto;

import java.time.LocalDateTime;

public class AuditoriaResponse {

    private Integer idRegistro;
    private LocalDateTime fechaHora;
    private String usuario;
    private String tipoAccion;
    private String entidadAfectada;
    private Integer idEntidadAfectada;
    private String descripcion;
    private String direccionIp;

    public Integer getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(Integer idRegistro) {
        this.idRegistro = idRegistro;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getTipoAccion() {
        return tipoAccion;
    }

    public void setTipoAccion(String tipoAccion) {
        this.tipoAccion = tipoAccion;
    }

    public String getEntidadAfectada() {
        return entidadAfectada;
    }

    public void setEntidadAfectada(String entidadAfectada) {
        this.entidadAfectada = entidadAfectada;
    }

    public Integer getIdEntidadAfectada() {
        return idEntidadAfectada;
    }

    public void setIdEntidadAfectada(Integer idEntidadAfectada) {
        this.idEntidadAfectada = idEntidadAfectada;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }
}
