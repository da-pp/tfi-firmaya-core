package com.firmaya.core.exception;

/**
 * Falla interna con un mensaje definido por el caso de uso
 * (por ejemplo "El PDF no pudo ser generado en este momento."). Se responde con 500.
 */
public class ErrorInternoException extends RuntimeException {

    public ErrorInternoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
