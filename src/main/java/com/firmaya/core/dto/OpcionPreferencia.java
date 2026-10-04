package com.firmaya.core.dto;

import javax.validation.constraints.NotBlank;

/**
 * CU-20: un evento (interruptor) o un canal (casilla) con su estado activo/inactivo.
 */
public class OpcionPreferencia {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private boolean activo;

    public OpcionPreferencia() {
    }

    public OpcionPreferencia(String nombre, boolean activo) {
        this.nombre = nombre;
        this.activo = activo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
