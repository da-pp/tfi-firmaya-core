package com.firmaya.core.dto;

import javax.validation.constraints.Size;

/**
 * CU-14 paso 9: razón de la restauración (opcional).
 */
public class RestaurarVersionRequest {

    @Size(max = 500, message = "La razón de la restauración no puede superar los 500 caracteres")
    private String razon;

    public String getRazon() {
        return razon;
    }

    public void setRazon(String razon) {
        this.razon = razon;
    }
}
