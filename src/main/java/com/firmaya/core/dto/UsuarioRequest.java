package com.firmaya.core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * CU-15 pasos 5 a 9: formulario de alta y edición de usuario.
 */
public class UsuarioRequest {

    @NotBlank(message = "El campo Nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "El nombre solo puede contener letras y espacios")
    private String nombre;

    @NotBlank(message = "El campo Apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar los 100 caracteres")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "El apellido solo puede contener letras y espacios")
    private String apellido;

    @NotBlank(message = "El campo Email es obligatorio")
    @Size(max = 254, message = "El email no puede superar los 254 caracteres")
    @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Ingrese un correo electrónico válido")
    private String email;

    @NotNull(message = "El campo Rol es obligatorio")
    private Integer idRol;

    @NotBlank(message = "El campo Estado es obligatorio")
    @Pattern(regexp = "^(Activo|Inactivo)$", message = "El estado debe ser Activo o Inactivo")
    private String estado;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
