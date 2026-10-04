package com.firmaya.core.service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.ActividadResponse;
import com.firmaya.core.dto.ConteoEstadoResponse;
import com.firmaya.core.dto.ContratoResumenResponse;
import com.firmaya.core.dto.ContratosPaginaResponse;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.ContratoRepository;
import com.firmaya.core.repository.EstadoContratoRepository;
import com.firmaya.core.repository.VersionContratoRepository;

/**
 * CU-17 – Ver Panel de Actividad Global.
 *
 * Definiciones confirmadas:
 * - Última modificación: la última VERSION_CONTRATO.fecha_creacion. El período se aplica sobre ella.
 * - Activos: no Archivados. Pendientes de firma: "Listo para firmar".
 * - Firmados en el período: la última FIRMA.fecha_firma cae en el período.
 * - Próximos a vencer: fecha_expiracion dentro de los próximos 7 días.
 * - Responsable: el creador del contrato.
 */
@Service
public class ActividadService {

    public static final String METRICA_ACTIVOS = "ACTIVOS";
    public static final String METRICA_PENDIENTES_FIRMA = "PENDIENTES_FIRMA";
    public static final String METRICA_FIRMADOS = "FIRMADOS";
    public static final String METRICA_PROXIMOS_A_VENCER = "PROXIMOS_A_VENCER";

    static final String MENSAJE_SIN_ACTIVIDAD = "No se registró actividad en el período seleccionado.";
    static final String MENSAJE_RANGO_INVALIDO = "La fecha de inicio debe ser anterior a la fecha de fin.";

    private static final int DIAS_PERIODO_PREDETERMINADO = 30;
    private static final int DIAS_PROXIMOS_A_VENCER = 7;
    private static final int CONTRATOS_RECIENTES = 10;
    private static final int CONTRATOS_POR_PAGINA = 20;
    private static final DateTimeFormatter FORMATO_CSV = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ContratoRepository contratoRepository;
    private final VersionContratoRepository versionContratoRepository;
    private final EstadoContratoRepository estadoContratoRepository;

    public ActividadService(ContratoRepository contratoRepository,
            VersionContratoRepository versionContratoRepository, EstadoContratoRepository estadoContratoRepository) {
        this.contratoRepository = contratoRepository;
        this.versionContratoRepository = versionContratoRepository;
        this.estadoContratoRepository = estadoContratoRepository;
    }

    /**
     * Pasos 2 a 17: métricas, gráfico por estado y los 10 contratos más recientemente activos.
     */
    @Transactional(readOnly = true)
    public ActividadResponse obtenerActividad(LocalDate fechaInicio, LocalDate fechaFin, List<String> estados) {
        Periodo periodo = resolverPeriodo(fechaInicio, fechaFin);
        DatosPanel datos = cargarDatos();

        List<Contrato> delPeriodo = contratosDelPeriodo(datos, periodo, estados);
        List<Contrato> firmados = contratosDeMetrica(METRICA_FIRMADOS, datos, periodo, estados);

        ActividadResponse response = new ActividadResponse();
        response.setFechaInicio(periodo.inicio);
        response.setFechaFin(periodo.fin);
        response.setContratosActivos(contratosDeMetrica(METRICA_ACTIVOS, datos, periodo, estados).size());
        response.setContratosPendientesFirma(
                contratosDeMetrica(METRICA_PENDIENTES_FIRMA, datos, periodo, estados).size());
        response.setContratosFirmados(firmados.size());
        response.setContratosProximosAVencer(
                contratosDeMetrica(METRICA_PROXIMOS_A_VENCER, datos, periodo, estados).size());
        response.setContratosPorEstado(contarPorEstado(delPeriodo));

        List<ContratoResumenResponse> recientes = new ArrayList<>();
        for (int i = 0; i < delPeriodo.size() && i < CONTRATOS_RECIENTES; i++) {
            recientes.add(toResumen(delPeriodo.get(i), datos));
        }
        response.setContratosRecientes(recientes);

        if (delPeriodo.isEmpty() && firmados.isEmpty()) {
            response.setMensaje(MENSAJE_SIN_ACTIVIDAD);
        }
        return response;
    }

    /**
     * Pasos 18 a 21: contratos de la métrica seleccionada, 20 por página.
     */
    @Transactional(readOnly = true)
    public ContratosPaginaResponse listarContratosDeMetrica(String metrica, LocalDate fechaInicio,
            LocalDate fechaFin, List<String> estados, int pagina) {
        if (pagina < 1) {
            throw new ReglaNegocioException("La página debe ser mayor o igual a 1");
        }
        Periodo periodo = resolverPeriodo(fechaInicio, fechaFin);
        DatosPanel datos = cargarDatos();
        List<Contrato> contratos = contratosDeMetrica(metrica, datos, periodo, estados);

        int desde = Math.min((pagina - 1) * CONTRATOS_POR_PAGINA, contratos.size());
        int hasta = Math.min(desde + CONTRATOS_POR_PAGINA, contratos.size());
        List<ContratoResumenResponse> paginaContratos = new ArrayList<>();
        for (Contrato contrato : contratos.subList(desde, hasta)) {
            paginaContratos.add(toResumen(contrato, datos));
        }

        ContratosPaginaResponse response = new ContratosPaginaResponse();
        response.setTotal(contratos.size());
        response.setPagina(pagina);
        response.setTotalPaginas((contratos.size() + CONTRATOS_POR_PAGINA - 1) / CONTRATOS_POR_PAGINA);
        response.setContratos(paginaContratos);
        return response;
    }

    /**
     * Pasos 22 a 24: CSV con los datos del período, los estados y la métrica seleccionados.
     */
    @Transactional(readOnly = true)
    public String exportarCsv(String metrica, LocalDate fechaInicio, LocalDate fechaFin, List<String> estados) {
        Periodo periodo = resolverPeriodo(fechaInicio, fechaFin);
        DatosPanel datos = cargarDatos();

        StringBuilder csv = new StringBuilder();
        csv.append("Nombre,Estado,Responsable,Última modificación\n");
        for (Contrato contrato : contratosDeMetrica(metrica, datos, periodo, estados)) {
            ContratoResumenResponse fila = toResumen(contrato, datos);
            csv.append(Csv.campo(fila.getNombre())).append(',');
            csv.append(Csv.campo(fila.getEstado())).append(',');
            csv.append(Csv.campo(fila.getResponsable())).append(',');
            csv.append(Csv.campo(fila.getUltimaModificacion() == null ? null
                    : fila.getUltimaModificacion().format(FORMATO_CSV))).append('\n');
        }
        return csv.toString();
    }

    // Pasos 2, 11, 12 y 14: período predeterminado de 30 días o el rango ingresado
    private Periodo resolverPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null && fechaFin == null) {
            LocalDate hoy = LocalDate.now();
            return new Periodo(hoy.minusDays(DIAS_PERIODO_PREDETERMINADO), hoy);
        }
        if (fechaInicio == null) {
            throw new ReglaNegocioException("fechaInicio", "El campo Fecha de inicio es obligatorio");
        }
        if (fechaFin == null) {
            throw new ReglaNegocioException("fechaFin", "El campo Fecha de fin es obligatorio");
        }
        if (!fechaInicio.isBefore(fechaFin)) {
            throw new ReglaNegocioException("fechaInicio", MENSAJE_RANGO_INVALIDO);
        }
        return new Periodo(fechaInicio, fechaFin);
    }

    private DatosPanel cargarDatos() {
        DatosPanel datos = new DatosPanel();
        datos.contratos = contratoRepository.findAll();
        for (Object[] fila : versionContratoRepository.buscarUltimaModificacionPorContrato()) {
            datos.ultimaModificacion.put(((Number) fila[0]).intValue(), aFechaHora(fila[1]));
        }
        for (Object[] fila : contratoRepository.buscarUltimaFirmaPorContrato()) {
            datos.ultimaFirma.put(((Number) fila[0]).intValue(), aFechaHora(fila[1]));
        }
        return datos;
    }

    // Contratos modificados en el período y con estado dentro del filtro, del más reciente al más antiguo
    private List<Contrato> contratosDelPeriodo(DatosPanel datos, Periodo periodo, List<String> estados) {
        List<Contrato> resultado = new ArrayList<>();
        for (Contrato contrato : datos.contratos) {
            LocalDateTime ultimaModificacion = datos.ultimaModificacion.get(contrato.getIdContrato());
            if (periodo.contiene(ultimaModificacion) && estadoIncluido(contrato, estados)) {
                resultado.add(contrato);
            }
        }
        resultado.sort((a, b) -> datos.ultimaModificacion.get(b.getIdContrato())
                .compareTo(datos.ultimaModificacion.get(a.getIdContrato())));
        return resultado;
    }

    private List<Contrato> contratosDeMetrica(String metrica, DatosPanel datos, Periodo periodo,
            List<String> estados) {
        if (METRICA_FIRMADOS.equals(metrica)) {
            return contratosFirmados(datos, periodo, estados);
        }

        LocalDate hoy = LocalDate.now();
        List<Contrato> resultado = new ArrayList<>();
        for (Contrato contrato : contratosDelPeriodo(datos, periodo, estados)) {
            String estado = nombreEstado(contrato);
            if (METRICA_ACTIVOS.equals(metrica)) {
                if (!EstadoContrato.ARCHIVADO.equals(estado)) {
                    resultado.add(contrato);
                }
            } else if (METRICA_PENDIENTES_FIRMA.equals(metrica)) {
                if (EstadoContrato.LISTO_PARA_FIRMAR.equals(estado)) {
                    resultado.add(contrato);
                }
            } else if (METRICA_PROXIMOS_A_VENCER.equals(metrica)) {
                LocalDate vencimiento = contrato.getFechaExpiracion();
                if (vencimiento != null && !vencimiento.isBefore(hoy)
                        && !vencimiento.isAfter(hoy.plusDays(DIAS_PROXIMOS_A_VENCER))) {
                    resultado.add(contrato);
                }
            } else {
                throw new ReglaNegocioException("metrica", "La métrica seleccionada no es válida");
            }
        }
        return resultado;
    }

    // Firmados en el período: la última firma cae en el período y el contrato quedó firmado
    // (Firmado, o Archivado, que solo se alcanza desde Firmado)
    private List<Contrato> contratosFirmados(DatosPanel datos, Periodo periodo, List<String> estados) {
        List<Contrato> resultado = new ArrayList<>();
        for (Contrato contrato : datos.contratos) {
            String estado = nombreEstado(contrato);
            boolean firmado = EstadoContrato.FIRMADO.equals(estado) || EstadoContrato.ARCHIVADO.equals(estado);
            if (firmado && periodo.contiene(datos.ultimaFirma.get(contrato.getIdContrato()))
                    && estadoIncluido(contrato, estados)) {
                resultado.add(contrato);
            }
        }
        resultado.sort((a, b) -> datos.ultimaFirma.get(b.getIdContrato())
                .compareTo(datos.ultimaFirma.get(a.getIdContrato())));
        return resultado;
    }

    // Paso 8: cantidad por estado, incluyendo los estados sin contratos
    private List<ConteoEstadoResponse> contarPorEstado(List<Contrato> contratos) {
        List<ConteoEstadoResponse> conteos = new ArrayList<>();
        for (EstadoContrato estado : estadoContratoRepository.findAll(Sort.by("idEstado"))) {
            long cantidad = 0;
            for (Contrato contrato : contratos) {
                if (estado.getNombre().equals(nombreEstado(contrato))) {
                    cantidad++;
                }
            }
            conteos.add(new ConteoEstadoResponse(estado.getNombre(), cantidad));
        }
        return conteos;
    }

    // Paso 13: sin estados seleccionados no se filtra por estado
    private boolean estadoIncluido(Contrato contrato, List<String> estados) {
        return estados == null || estados.isEmpty() || estados.contains(nombreEstado(contrato));
    }

    private String nombreEstado(Contrato contrato) {
        return contrato.getEstado() == null ? null : contrato.getEstado().getNombre();
    }

    private LocalDateTime aFechaHora(Object valor) {
        if (valor instanceof Timestamp) {
            return ((Timestamp) valor).toLocalDateTime();
        }
        return (LocalDateTime) valor;
    }

    private ContratoResumenResponse toResumen(Contrato contrato, DatosPanel datos) {
        ContratoResumenResponse resumen = new ContratoResumenResponse();
        resumen.setIdContrato(contrato.getIdContrato());
        resumen.setNombre(contrato.getNombre());
        resumen.setEstado(nombreEstado(contrato));
        if (contrato.getUsuarioCreador() != null) {
            resumen.setResponsable(contrato.getUsuarioCreador().getNombre() + " "
                    + contrato.getUsuarioCreador().getApellido());
        }
        resumen.setUltimaModificacion(datos.ultimaModificacion.get(contrato.getIdContrato()));
        return resumen;
    }

    /** Rango de días del panel, ambos extremos incluidos. */
    private static class Periodo {
        private final LocalDate inicio;
        private final LocalDate fin;

        Periodo(LocalDate inicio, LocalDate fin) {
            this.inicio = inicio;
            this.fin = fin;
        }

        boolean contiene(LocalDateTime fechaHora) {
            if (fechaHora == null) {
                return false;
            }
            LocalDate fecha = fechaHora.toLocalDate();
            return !fecha.isBefore(inicio) && !fecha.isAfter(fin);
        }
    }

    /** Datos leídos una sola vez por consulta del panel. */
    private static class DatosPanel {
        private List<Contrato> contratos;
        private final Map<Integer, LocalDateTime> ultimaModificacion = new HashMap<>();
        private final Map<Integer, LocalDateTime> ultimaFirma = new HashMap<>();
    }
}
