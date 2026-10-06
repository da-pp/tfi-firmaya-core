package com.firmaya.core.service;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

/**
 * Genera valores aleatorios seguros para sesiones y enlaces tokenizados.
 */
@Component
public class GeneradorToken {

    private final SecureRandom secureRandom = new SecureRandom();

    public String generar() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Código OTP numérico de 6 dígitos.
     */
    public String generarCodigoOtp() {
        return String.format("%06d", secureRandom.nextInt(1000000));
    }
}
