package com.firmaya.core.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.AuditoriaDetalleResponse;
import com.firmaya.core.dto.AuditoriaPaginaResponse;
import com.firmaya.core.dto.AuditoriaResponse;
import com.firmaya.core.entity.Auditoria;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.VersionContrato;
import com.firmaya.core.exception.RecursoNoEncontradoException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.AuditoriaRepository;
import com.firmaya.core.repository.ContratoRepository;
import com.firmaya.core.repository.VersionContratoRepository;

/**
 * CU-18 – Registro de acciones de auditoría (consulta, detalle y exportación CSV).
 */
@Service
public class AuditoriaConsultaService {

    public static final String ENTIDAD_CONTRATO = "contrato";
    static final String MENSAJE_SIN_RESULTADOS = "No se encontraron registros con los filtros aplicados.";

    private static final int REGISTROS_POR_PAGINA = 50;
    private static final DateTimeFormatter FORMATO_CSV = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final AuditoriaRepository auditoriaRepository;
    private final ContratoRepository contratoRepository;
    private final VersionContratoRepository versionContratoRepository;

    public AuditoriaConsultaService(AuditoriaRepository auditoriaRepository, ContratoRepository contratoRepository,
            VersionContratoRepository versionContratoRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.contratoRepository = contratoRepository;
        this.versionContratoRepository = versionContratoRepository;
    }

    /**
     * Pasos 2 y 10 a 20. Sin filtros devuelve las últimas 50 acciones.
     */
    @Transactional(readOnly = true)
    public AuditoriaPaginaResponse buscar(LocalDate fechaDesde, LocalDate fechaHasta, String usuario,
            String tipoAccion, String contrato, int pagina) {
        if (pagina < 1) {
            throw new ReglaNegocioException("La página debe ser mayor o igual a 1");
        }

        Specification<Auditoria> filtro = construirFiltro(fechaDesde, fechaHasta, usuario, tipoAccion, contrato);
        PageRequest pageRequest = PageRequest.of(pagina - 1, REGISTROS_POR_PAGINA, ordenMasReciente());
        Page<Auditoria> resultado = auditoriaRepository.findAll(filtro, pageRequest);

        List<AuditoriaResponse> registros = new ArrayList<>();
        for (Auditoria auditoria : resultado.getContent()) {
            registros.add(toResponse(auditoria));
        }

        AuditoriaPaginaResponse response = new AuditoriaPaginaResponse();
        response.setTotal(resultado.getTotalElements());
        response.setPagina(pagina);
        response.setTotalPaginas(resultado.getTotalPages());
        response.setRegistros(registros);
        if (resultado.getTotalElements() == 0) {
            response.setMensaje(MENSAJE_SIN_RESULTADOS);
        } else {
            response.setMensaje(resultado.getTotalElements() + " registros encontrados.");
        }
        return response;
    }

    /**
     * Paso 18: datos antes y después, versión del contrato afectado y hash.
     */
    @Transactional(readOnly = true)
    public AuditoriaDetalleResponse obtenerDetalle(Integer idRegistro) {
        Auditoria auditoria = auditoriaRepository.findById(idRegistro)
                .orElseThrow(() -> new RecursoNoEncontradoException("Registro de auditoría no encontrado"));

        AuditoriaDetalleResponse detalle = new AuditoriaDetalleResponse();
        detalle.setRegistro(toResponse(auditoria));
        detalle.setDatosAntes(auditoria.getDatosAntes());
        detalle.setDatosDespues(auditoria.getDatosDespues());

        if (auditoria.getIdVersion() != null) {
            VersionContrato version = versionContratoRepository.findById(auditoria.getIdVersion()).orElse(null);
            if (version != null) {
                detalle.setNumeroVersion(version.getNumeroVersion());
                detalle.setHashVersion(version.getHashSha256());
            }
        }
        return detalle;
    }

    /**
     * Pasos 21 y 22: CSV con todos los registros que cumplen los filtros activos.
     */
    @Transactional(readOnly = true)
    public String exportarCsv(LocalDate fechaDesde, LocalDate fechaHasta, String usuario, String tipoAccion,
            String contrato) {
        Specification<Auditoria> filtro = construirFiltro(fechaDesde, fechaHasta, usuario, tipoAccion, contrato);
        List<Auditoria> registros = auditoriaRepository.findAll(filtro, ordenMasReciente());

        StringBuilder csv = new StringBuilder();
        csv.append("Fecha y hora,Usuario,Tipo de acción,Entidad afectada,Descripción,Dirección IP\n");
        for (Auditoria auditoria : registros) {
            AuditoriaResponse fila = toResponse(auditoria);
            csv.append(Csv.campo(fila.getFechaHora() == null ? null : fila.getFechaHora().format(FORMATO_CSV))).append(',');
            csv.append(Csv.campo(fila.getUsuario())).append(',');
            csv.append(Csv.campo(fila.getTipoAccion())).append(',');
            csv.append(Csv.campo(fila.getEntidadAfectada())).append(',');
            csv.append(Csv.campo(fila.getDescripcion())).append(',');
            csv.append(Csv.campo(fila.getDireccionIp())).append('\n');
        }
        return csv.toString();
    }

    private Specification<Auditoria> construirFiltro(LocalDate fechaDesde, LocalDate fechaHasta, String usuario,
            String tipoAccion, String contrato) {
        // El filtro por contrato busca primero los contratos cuyo nombre contiene el texto
        List<Integer> idsContrato = null;
        if (tieneTexto(contrato)) {
            idsContrato = new ArrayList<>();
            for (Contrato encontrado : contratoRepository.findByNombreContainingIgnoreCase(contrato.trim())) {
                idsContrato.add(encontrado.getIdContrato());
            }
        }
        final List<Integer> idsContratoFiltro = idsContrato;

        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();

            if (fechaDesde != null) {
                condiciones.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), fechaDesde.atStartOfDay()));
            }
            if (fechaHasta != null) {
                condiciones.add(cb.lessThan(root.get("fechaHora"), fechaHasta.plusDays(1).atStartOfDay()));
            }
            if (tieneTexto(usuario)) {
                Join<Auditoria, Usuario> joinUsuario = root.join("usuario", JoinType.LEFT);
                String patron = "%" + usuario.trim().toLowerCase() + "%";
                condiciones.add(cb.or(
                        cb.like(cb.lower(joinUsuario.get("email")), patron),
                        cb.like(cb.lower(joinUsuario.get("nombre")), patron),
                        cb.like(cb.lower(joinUsuario.get("apellido")), patron)));
            }
            if (tieneTexto(tipoAccion)) {
                condiciones.add(cb.equal(root.get("tipoAccion"), tipoAccion.trim()));
            }
            if (idsContratoFiltro != null) {
                if (idsContratoFiltro.isEmpty()) {
                    // Ningún contrato coincide con el texto buscado
                    condiciones.add(cb.disjunction());
                } else {
                    condiciones.add(cb.equal(root.get("entidadAfectada"), ENTIDAD_CONTRATO));
                    condiciones.add(root.get("idEntidadAfectada").in(idsContratoFiltro));
                }
            }

            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }

    private Sort ordenMasReciente() {
        return Sort.by(Sort.Direction.DESC, "fechaHora", "idRegistro");
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private AuditoriaResponse toResponse(Auditoria auditoria) {
        AuditoriaResponse response = new AuditoriaResponse();
        response.setIdRegistro(auditoria.getIdRegistro());
        response.setFechaHora(auditoria.getFechaHora());
        response.setUsuario(auditoria.getUsuario() == null ? null : auditoria.getUsuario().getEmail());
        response.setTipoAccion(auditoria.getTipoAccion());
        response.setEntidadAfectada(auditoria.getEntidadAfectada());
        response.setIdEntidadAfectada(auditoria.getIdEntidadAfectada());
        response.setDescripcion(auditoria.getDescripcion());
        response.setDireccionIp(auditoria.getDireccionIp());
        return response;
    }
}
