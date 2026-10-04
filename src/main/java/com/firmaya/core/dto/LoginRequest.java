package com.firmaya.core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * CU-19 pasos 3, 4, 12, 13 y 14.
 */
public class LoginRequest {

    @NotBlank(message = "El campo Correo electrónico es obligatorio")
    @Size(max = 254, message = "El correo electrónico no puede superar los 254 caracteres")
    @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Ingrese un correo electrónico válido")
    private String email;

    @NotBlank(message = "El campo Contraseña es obligatorio")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String contrasenia;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }
}
