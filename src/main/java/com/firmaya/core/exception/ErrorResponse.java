package com.firmaya.core.exception;

import java.util.LinkedHashMap;
import java.util.Map;

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
