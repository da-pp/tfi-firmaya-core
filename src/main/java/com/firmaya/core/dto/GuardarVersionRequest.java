package com.firmaya.core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class GuardarVersionRequest {

    @NotBlank(message = "El contenido del contrato debe tener al menos 100 caracteres")
    private String contenido;

    @Size(max = 500, message = "El comentario de versión no puede superar los 500 caracteres")
    private String comentario;

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
