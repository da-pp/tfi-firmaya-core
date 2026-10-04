package com.firmaya.core.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.CompararVersionesResponse;
import com.firmaya.core.dto.FragmentoComparacion;
import com.firmaya.core.dto.HistorialVersionesResponse;
import com.firmaya.core.dto.VerificarIntegridadResponse;
import com.firmaya.core.dto.VersionGuardadaResponse;
import com.firmaya.core.dto.VersionResponse;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.VersionContrato;
import com.firmaya.core.exception.RecursoNoEncontradoException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.VersionContratoRepository;

/**
 * CU-11 Ver historial, CU-12 Comparar versiones, CU-13 Verificar integridad y CU-14 Restaurar versión.
 */
@Service
public class VersionService {

    static final String MENSAJE_SIN_VERSIONES_ANTERIORES = "Este contrato aún no tiene versiones anteriores.";
    static final String MENSAJE_RESTAURACION_NO_DISPONIBLE =
            "La restauración de versiones no está disponible en el estado actual del contrato.";
    static final String MENSAJE_MISMA_VERSION = "Seleccione dos versiones diferentes para realizar la comparación.";
    static final String MENSAJE_SIN_DIFERENCIAS = "Las versiones seleccionadas no presentan diferencias en el contenido.";
    static final String MENSAJE_INTEGRIDAD_OK = "Integridad verificada. El documento no ha sido alterado.";
    static final String MENSAJE_INTEGRIDAD_ERROR = "Los hashes no coinciden. El documento puede haber sido "
            + "modificado o está examinando una versión diferente.";

    private final ContratoService contratoService;
    private final VersionContratoRepository versionContratoRepository;
    private final AuditoriaService auditoriaService;

    public VersionService(ContratoService contratoService, VersionContratoRepository versionContratoRepository,
            AuditoriaService auditoriaService) {
        this.contratoService = contratoService;
        this.versionContratoRepository = versionContratoRepository;
        this.auditoriaService = auditoriaService;
    }

    /**
     * CU-11 pasos 2 a 9 (y CU-14 paso 2: disponibilidad de "Restaurar esta versión").
     */
    @Transactional(readOnly = true)
    public HistorialVersionesResponse obtenerHistorial(Integer idContrato) {
        Contrato contrato = contratoService.buscarContrato(idContrato);
        List<VersionContrato> versiones = versionContratoRepository
                .findByContratoIdContratoOrderByNumeroVersionDesc(idContrato);

        List<VersionResponse> lista = new ArrayList<>();
        for (int i = 0; i < versiones.size(); i++) {
            // La primera de la lista es la más reciente: la versión actual
            lista.add(toResponse(versiones.get(i), i == 0, false));
        }

        HistorialVersionesResponse response = new HistorialVersionesResponse();
        response.setVersiones(lista);
        if (versiones.size() == 1) {
            response.setMensaje(MENSAJE_SIN_VERSIONES_ANTERIORES);
        }
        response.setRestauracionDisponible(restauracionPermitida(contrato));
        if (!response.isRestauracionDisponible()) {
            response.setMensajeRestauracion(MENSAJE_RESTAURACION_NO_DISPONIBLE);
        }
        return response;
    }

    /**
     * CU-11 pasos 14 y 15: contenido de una versión en modo solo lectura.
     */
    @Transactional(readOnly = true)
    public VersionResponse obtenerVersion(Integer idContrato, Integer numeroVersion) {
        VersionContrato version = buscarVersion(idContrato, numeroVersion);
        VersionContrato actual = contratoService.versionActual(idContrato);
        return toResponse(version, version.getNumeroVersion().equals(actual.getNumeroVersion()), true);
    }

    /**
     * CU-12 pasos 7 a 15.
     */
    @Transactional(readOnly = true)
    public CompararVersionesResponse compararVersiones(Integer idContrato, Integer numeroA, Integer numeroB) {
        if (numeroA.equals(numeroB)) {
            throw new ReglaNegocioException(MENSAJE_MISMA_VERSION);
        }
        VersionContrato versionA = buscarVersion(idContrato, numeroA);
        VersionContrato versionB = buscarVersion(idContrato, numeroB);
        Integer numeroActual = contratoService.versionActual(idContrato).getNumeroVersion();

        List<FragmentoComparacion> fragmentos = ComparadorTexto.comparar(
                ContenidoHtml.textoPlano(versionA.getContenido()), ContenidoHtml.textoPlano(versionB.getContenido()));
        int cambios = ComparadorTexto.contarCambios(fragmentos);

        CompararVersionesResponse response = new CompararVersionesResponse();
        response.setVersionA(toResponse(versionA, versionA.getNumeroVersion().equals(numeroActual), false));
        response.setVersionB(toResponse(versionB, versionB.getNumeroVersion().equals(numeroActual), false));
        response.setFragmentos(fragmentos);
        response.setCantidadCambios(cambios);
        if (cambios == 0) {
            response.setMensaje(MENSAJE_SIN_DIFERENCIAS);
        } else {
            response.setMensaje(cambios + " cambios detectados entre las versiones seleccionadas.");
        }
        return response;
    }

    /**
     * CU-13 pasos 9 a 16: compara el hash ingresado con el de la versión activa y audita el resultado.
     */
    @Transactional
    public VerificarIntegridadResponse verificarIntegridad(Integer idContrato, String hashIngresado, Usuario usuario,
            String direccionIp) {
        contratoService.buscarContrato(idContrato);
        VersionContrato actual = contratoService.versionActual(idContrato);
        boolean coincide = actual.getHashSha256().equals(hashIngresado);

        VerificarIntegridadResponse response = new VerificarIntegridadResponse();
        response.setCoincide(coincide);
        response.setHashIngresado(hashIngresado);
        response.setHashAlmacenado(actual.getHashSha256());
        response.setNumeroVersion(actual.getNumeroVersion());
        response.setMensaje(coincide ? MENSAJE_INTEGRIDAD_OK : MENSAJE_INTEGRIDAD_ERROR);

        auditoriaService.registrarEnContrato(usuario, "Verificación de integridad", idContrato,
                actual.getIdVersion(), "Verificación de integridad de la versión " + actual.getNumeroVersion()
                        + ": " + (coincide ? "hashes coinciden" : "hashes no coinciden"),
                direccionIp, null, null);
        return response;
    }

    /**
     * CU-14 pasos 14 a 19: crea una nueva versión con el contenido de la versión seleccionada.
     */
    @Transactional
    public VersionGuardadaResponse restaurarVersion(Integer idContrato, Integer numeroVersion, String razon,
            Usuario usuario, String direccionIp) {
        Contrato contrato = contratoService.buscarContrato(idContrato);
        if (!restauracionPermitida(contrato)) {
            throw new ReglaNegocioException(MENSAJE_RESTAURACION_NO_DISPONIBLE);
        }

        VersionContrato seleccionada = buscarVersion(idContrato, numeroVersion);
        VersionContrato actual = contratoService.versionActual(idContrato);
        if (seleccionada.getNumeroVersion() >= actual.getNumeroVersion()) {
            throw new ReglaNegocioException("Solo se pueden restaurar versiones anteriores a la versión actual");
        }

        // Pasos 15 a 17: siguiente número, hash nuevo y comentario de restauración
        VersionContrato nueva = contratoService.guardarNuevaVersion(contrato, usuario,
                actual.getNumeroVersion() + 1, seleccionada.getContenido(),
                "Restauración de la versión " + numeroVersion, razon);

        auditoriaService.registrarEnContrato(usuario, "Restauración", idContrato, nueva.getIdVersion(),
                "Restauración de la versión " + numeroVersion + " como versión " + nueva.getNumeroVersion(),
                direccionIp, "Versión " + actual.getNumeroVersion() + " | Hash: " + actual.getHashSha256(),
                "Versión " + nueva.getNumeroVersion() + " | Hash: " + nueva.getHashSha256());

        // Paso 19
        VersionGuardadaResponse response = new VersionGuardadaResponse();
        response.setIdContrato(idContrato);
        response.setEstado(contrato.getEstado().getNombre());
        response.setNumeroVersion(nueva.getNumeroVersion());
        response.setHash(nueva.getHashSha256());
        response.setMensaje("La versión " + numeroVersion + " fue restaurada exitosamente como la nueva versión "
                + nueva.getNumeroVersion() + ".");
        return response;
    }

    // CU-14 precondición: el contrato no está Firmado ni Archivado
    private boolean restauracionPermitida(Contrato contrato) {
        String estado = contrato.getEstado().getNombre();
        return !EstadoContrato.FIRMADO.equals(estado) && !EstadoContrato.ARCHIVADO.equals(estado);
    }

    private VersionContrato buscarVersion(Integer idContrato, Integer numeroVersion) {
        return versionContratoRepository.findByContratoIdContratoAndNumeroVersion(idContrato, numeroVersion)
                .orElseThrow(() -> new RecursoNoEncontradoException("Versión " + numeroVersion + " no encontrada"));
    }

    private VersionResponse toResponse(VersionContrato version, boolean actual, boolean incluirContenido) {
        VersionResponse response = new VersionResponse();
        response.setNumeroVersion(version.getNumeroVersion());
        if (version.getUsuarioAutor() != null) {
            response.setAutor(version.getUsuarioAutor().getNombre() + " " + version.getUsuarioAutor().getApellido());
        }
        response.setFechaCreacion(version.getFechaCreacion());
        response.setComentario(version.getComentario());
        response.setRazonRestauracion(version.getRazonRestauracion());
        response.setHash(version.getHashSha256());
        response.setActual(actual);
        if (incluirContenido) {
            response.setContenido(version.getContenido());
        }
        return response;
    }
}
