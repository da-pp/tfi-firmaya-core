package com.firmaya.core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * CU-21 pasos 3 y 6.
 */
public class RecuperarContraseniaRequest {

    @NotBlank(message = "El campo Correo electrónico es obligatorio")
    @Size(max = 254, message = "El correo electrónico no puede superar los 254 caracteres")
    @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$",
            message = "Ingrese un correo electrónico válido (ej. nombre@dominio.com)")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
