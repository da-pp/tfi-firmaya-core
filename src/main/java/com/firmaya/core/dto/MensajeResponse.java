package com.firmaya.core.dto;

/**
 * Respuesta simple con el mensaje que el frontend muestra al usuario.
 */
public class MensajeResponse {

    private String mensaje;

    public MensajeResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }
}
