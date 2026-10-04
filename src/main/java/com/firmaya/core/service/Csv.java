package com.firmaya.core.service;

/**
 * Utilidad para armar archivos CSV (CU-17 y CU-18).
 */
public final class Csv {

    private Csv() {
    }

    // Encierra el valor entre comillas y duplica las comillas internas
    public static String campo(String valor) {
        if (valor == null) {
            return "";
        }
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }
}
