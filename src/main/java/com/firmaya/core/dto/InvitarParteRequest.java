package com.firmaya.core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * CU-03 pasos 4 a 7: formulario de invitación.
 */
public class InvitarParteRequest {

    @NotBlank(message = "El campo Correo electrónico es obligatorio")
    @Size(max = 254, message = "El correo electrónico no puede superar los 254 caracteres")
    @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$",
            message = "Ingrese un correo electrónico válido (ej. nombre@dominio.com)")
    private String email;

    @NotBlank(message = "El campo Nombre de la parte es obligatorio")
    @Size(max = 150, message = "El nombre de la parte no puede superar los 150 caracteres")
    private String nombre;

    @NotBlank(message = "El campo Rol es obligatorio")
    @Pattern(regexp = "^(Firmante|Solo lectura|Revisor)$", message = "El rol debe ser Firmante, Solo lectura o Revisor")
    private String rol;

    @Size(max = 500, message = "El mensaje personalizado no puede superar los 500 caracteres")
    private String mensaje;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
