package com.firmaya.core.dto;

/**
 * CU-03 pasos 15 y 16, y camino alternativo "Error al enviar el correo electrónico".
 */
public class InvitacionResponse {

    private ParteResponse parte;
    private boolean correoEnviado;
    private String mensaje;

    public ParteResponse getParte() {
        return parte;
    }

    public void setParte(ParteResponse parte) {
        this.parte = parte;
    }

    public boolean isCorreoEnviado() {
        return correoEnviado;
    }

    public void setCorreoEnviado(boolean correoEnviado) {
        this.correoEnviado = correoEnviado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
