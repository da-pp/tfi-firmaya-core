package com.firmaya.core.service;

public final class ContenidoHtml {

    private ContenidoHtml() {
    }

    public static String textoPlano(String html) {
        if (html == null) {
            return "";
        }
        String texto = html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</(p|div|li|h[1-6]|blockquote)>", "\n")
                .replaceAll("<[^>]*>", "")
                .replace("&nbsp;", " ")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&amp;", "&");
        return texto.trim();
    }

    /**
     * Escapa un texto para insertarlo dentro del contenido HTML.
     */
    public static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
