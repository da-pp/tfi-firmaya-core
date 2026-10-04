package com.firmaya.core.exception;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cuerpo de error común. "errores" contiene el mensaje de cada campo inválido
 * para que el frontend muestre el helper debajo de cada campo.
 */
public class ErrorResponse {

    private String mensaje;
    private Map<String, String> errores = new LinkedHashMap<>();

    public ErrorResponse(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
