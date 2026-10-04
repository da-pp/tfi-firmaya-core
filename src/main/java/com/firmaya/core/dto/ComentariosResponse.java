package com.firmaya.core.dto;

import java.util.List;

/**
 * CU-06: comentarios del contrato y contador del encabezado.
 * Al publicar, "comentario" trae el nuevo comentario y "mensaje" la confirmación.
 */
public class ComentariosResponse {

    private List<ComentarioResponse> comentarios;
    private ComentarioResponse comentario;
    private long totalComentarios;
    private String mensaje;

    public List<ComentarioResponse> getComentarios() {
        return comentarios;
    }

    public void setComentarios(List<ComentarioResponse> comentarios) {
        this.comentarios = comentarios;
    }

    public ComentarioResponse getComentario() {
        return comentario;
    }

    public void setComentario(ComentarioResponse comentario) {
        this.comentario = comentario;
    }

    public long getTotalComentarios() {
        return totalComentarios;
    }

    public void setTotalComentarios(long totalComentarios) {
        this.totalComentarios = totalComentarios;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
