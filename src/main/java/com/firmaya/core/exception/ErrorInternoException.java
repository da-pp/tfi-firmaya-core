package com.firmaya.core.exception;

public class ErrorInternoException extends RuntimeException {

    public ErrorInternoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
