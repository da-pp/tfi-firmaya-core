package com.firmaya.core.dto;

import java.time.LocalDateTime;

public class ComentarioResponse {

    private Integer idComentario;
    private String autor;
    private LocalDateTime fechaPublicacion;
    private String texto;
    private String textoSeleccionado;

    public Integer getIdComentario() {
        return idComentario;
    }

    public void setIdComentario(Integer idComentario) {
        this.idComentario = idComentario;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDateTime getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(LocalDateTime fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

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
