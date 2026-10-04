package com.firmaya.core.exception;

/**
 * Una regla de negocio de un caso de uso no se cumple. Se responde con 400.
 * Si se indica el campo, el mensaje se devuelve asociado a ese campo para mostrarlo como helper.
 */
public class ReglaNegocioException extends RuntimeException {

    private final String campo;

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
        this.campo = null;
    }

    public ReglaNegocioException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
