package com.firmaya.core.exception;

/**
 * El recurso solicitado no existe. Se responde con 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
