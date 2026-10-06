package com.firmaya.core.dto;

import javax.validation.constraints.NotBlank;

public class RestablecerContraseniaRequest {

    @NotBlank(message = "El token es obligatorio")
    private String token;

    @NotBlank(message = "El campo Nueva contraseña es obligatorio")
    private String nuevaContrasenia;

    @NotBlank(message = "El campo Confirmar nueva contraseña es obligatorio")
    private String confirmarContrasenia;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNuevaContrasenia() {
        return nuevaContrasenia;
    }

    public void setNuevaContrasenia(String nuevaContrasenia) {
        this.nuevaContrasenia = nuevaContrasenia;
    }

    public String getConfirmarContrasenia() {
        return confirmarContrasenia;
    }

    public void setConfirmarContrasenia(String confirmarContrasenia) {
        this.confirmarContrasenia = confirmarContrasenia;
    }
}
