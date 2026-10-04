package com.firmaya.core.dto;

import java.util.List;

/**
 * CU-17 pasos 19 a 21: contratos de una métrica, 20 por página.
 */
public class ContratosPaginaResponse {

    private long total;
    private int pagina;
    private int totalPaginas;
    private List<ContratoResumenResponse> contratos;

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

    public List<ContratoResumenResponse> getContratos() {
        return contratos;
    }

    public void setContratos(List<ContratoResumenResponse> contratos) {
        this.contratos = contratos;
    }
}
