package com.firmaya.core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * CU-05 pasos 4 y 8.
 */
public class CambiarEstadoRequest {

    @NotBlank(message = "El campo Nuevo estado es obligatorio")
    private String nuevoEstado;

    @Size(max = 500, message = "La razón del cambio no puede superar los 500 caracteres")
    private String razon;

    // Camino alternativo: el usuario eligió "Continuar" aunque no haya firmantes asignados
    private boolean continuarSinFirmantes;

    public String getNuevoEstado() {
        return nuevoEstado;
    }

    public void setNuevoEstado(String nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }

    public String getRazon() {
        return razon;
    }

    public void setRazon(String razon) {
        this.razon = razon;
    }

    public boolean isContinuarSinFirmantes() {
        return continuarSinFirmantes;
    }

    public void setContinuarSinFirmantes(boolean continuarSinFirmantes) {
        this.continuarSinFirmantes = continuarSinFirmantes;
    }
}
