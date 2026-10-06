package com.firmaya.core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ComentarioRequest {

    @NotBlank(message = "El comentario no puede estar vacío")
    @Size(max = 1000, message = "El comentario no puede superar los 1000 caracteres")
    private String texto;

    private String textoSeleccionado;

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getTextoSeleccionado() {
        return textoSeleccionado;
    }

    public void setTextoSeleccionado(String textoSeleccionado) {
        this.textoSeleccionado = textoSeleccionado;
    }
}
