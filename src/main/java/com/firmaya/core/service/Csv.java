package com.firmaya.core.service;

public final class Csv {

    private Csv() {
    }

    public static String campo(String valor) {
        if (valor == null) {
            return "";
        }
        return "\"" + valor.replace("\"", "\"\"") + "\"";
    }
}
