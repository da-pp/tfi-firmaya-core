package com.firmaya.core.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * CU-13 paso 4: hash a verificar.
 */
public class VerificarIntegridadRequest {

    public static final String MENSAJE_FORMATO = "El hash debe tener exactamente 64 caracteres hexadecimales (0-9, a-f).";

    @NotBlank(message = MENSAJE_FORMATO)
    @Pattern(regexp = "^[0-9a-f]{64}$", message = MENSAJE_FORMATO)
    private String hash;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
