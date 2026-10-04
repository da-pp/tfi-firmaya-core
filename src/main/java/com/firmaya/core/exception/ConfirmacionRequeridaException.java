package com.firmaya.core.exception;

/**
 * El caso de uso pide confirmar la acción antes de continuar (por ejemplo, guardar una plantilla
 * sin campos dinámicos). Se responde con 409; el frontend muestra el mensaje y, si el usuario
 * confirma, repite la solicitud indicando la confirmación.
 */
public class ConfirmacionRequeridaException extends RuntimeException {

    public ConfirmacionRequeridaException(String mensaje) {
        super(mensaje);
    }
}
