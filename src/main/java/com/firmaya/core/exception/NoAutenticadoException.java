package com.firmaya.core.exception;

/**
 * Credenciales o sesión inválidas. Se responde con 401.
 */
public class NoAutenticadoException extends RuntimeException {

    public NoAutenticadoException(String mensaje) {
        super(mensaje);
    }
}
