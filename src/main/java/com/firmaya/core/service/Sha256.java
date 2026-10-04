package com.firmaya.core.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hash SHA-256 expresado en 64 caracteres hexadecimales en minúscula.
 * Para el contenido de un contrato se usan los bytes UTF-8 del texto guardado.
 */
public final class Sha256 {

    private Sha256() {
    }

    public static String calcular(String contenido) {
        return calcular(contenido.getBytes(StandardCharsets.UTF_8));
    }

    public static String calcular(byte[] bytes) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(bytes);
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            // Toda JVM incluye SHA-256
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
