package com.firmaya.core.dto;

import java.util.List;

public class AuditoriaPaginaResponse {

    private long total;
    private int pagina;
    private int totalPaginas;
    private String mensaje;
    private List<AuditoriaResponse> registros;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPagina() {
        return pagina;
    }

    public void setPagina(int pagina) {
        this.pagina = pagina;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<AuditoriaResponse> getRegistros() {
        return registros;
    }

    public void setRegistros(List<AuditoriaResponse> registros) {
        this.registros = registros;
    }
}
