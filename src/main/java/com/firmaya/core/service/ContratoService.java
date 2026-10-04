package com.firmaya.core.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.ContratoDetalleResponse;
import com.firmaya.core.dto.ContratoResumenResponse;
import com.firmaya.core.dto.CrearContratoRequest;
import com.firmaya.core.dto.GuardarVersionRequest;
import com.firmaya.core.dto.VersionGuardadaResponse;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.Plantilla;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.VersionContrato;
import com.firmaya.core.exception.RecursoNoEncontradoException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.ContratoRepository;
import com.firmaya.core.repository.EstadoContratoRepository;
import com.firmaya.core.repository.PlantillaRepository;
import com.firmaya.core.repository.VersionContratoRepository;

/**
 * CU-01 – Crear contrato desde plantilla y CU-02 – Editar contrato en línea.
 */
@Service
public class ContratoService {

    static final String MENSAJE_CREADO = "Contrato creado exitosamente";
    static final String MENSAJE_VERSION_GUARDADA = "Versión guardada exitosamente";
    static final String MENSAJE_NO_EDITABLE = "Este contrato no puede ser editado en su estado actual.";
    static final String MENSAJE_CONTENIDO_MINIMO = "El contenido del contrato debe tener al menos 100 caracteres";

    private static final int CARACTERES_MINIMOS_CONTENIDO = 100;

    private final ContratoRepository contratoRepository;
    private final VersionContratoRepository versionContratoRepository;
    private final PlantillaRepository plantillaRepository;
    private final EstadoContratoRepository estadoContratoRepository;
    private final AuditoriaService auditoriaService;

    public ContratoService(ContratoRepository contratoRepository,
            VersionContratoRepository versionContratoRepository, PlantillaRepository plantillaRepository,
            EstadoContratoRepository estadoContratoRepository, AuditoriaService auditoriaService) {
        this.contratoRepository = contratoRepository;
        this.versionContratoRepository = versionContratoRepository;
        this.plantillaRepository = plantillaRepository;
        this.estadoContratoRepository = estadoContratoRepository;
        this.auditoriaService = auditoriaService;
    }

    /**
     * CU-01 pasos 16 a 20.
     */
    @Transactional
    public VersionGuardadaResponse crearContrato(CrearContratoRequest request, Usuario usuario, String direccionIp) {
        // Paso 16: formato DD/MM/AAAA y fecha de inicio no anterior a hoy
        LocalDate fechaInicio = Fechas.leer(request.getFechaInicio(), "fechaInicio",
                CrearContratoRequest.MENSAJE_FECHA_INICIO);
        if (fechaInicio.isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("fechaInicio", CrearContratoRequest.MENSAJE_FECHA_INICIO);
        }
        LocalDate fechaExpiracion = null;
        if (tieneTexto(request.getFechaExpiracion())) {
            fechaExpiracion = Fechas.leer(request.getFechaExpiracion(), "fechaExpiracion",
                    "La fecha debe tener el formato DD/MM/AAAA");
        }

        Plantilla plantilla = plantillaRepository.findById(request.getIdPlantilla())
                .orElseThrow(() -> new RecursoNoEncontradoException("Plantilla no encontrada"));
        if (!Plantilla.ESTADO_ACTIVA.equals(plantilla.getEstado())) {
            throw new ReglaNegocioException("idPlantilla", "La plantilla seleccionada no está activa");
        }

        // Paso 19: contrato en estado Borrador asociado al usuario creador
        Contrato contrato = new Contrato();
        contrato.setNombre(request.getNombre().trim());
        contrato.setPartesInvolucradas(request.getPartesInvolucradas().trim());
        contrato.setFechaInicio(fechaInicio);
        contrato.setFechaExpiracion(fechaExpiracion);
        contrato.setDescripcionPropiedad(request.getDescripcionPropiedad());
        contrato.setPlantilla(plantilla);
        contrato.setUsuarioCreador(usuario);
        contrato.setEstado(buscarEstado(EstadoContrato.BORRADOR));
        contratoRepository.save(contrato);

        // Pasos 17 y 18: versión 1 generada desde la plantilla, con su hash SHA-256
        String contenido = generarContenidoInicial(plantilla.getCuerpo(), contrato);
        VersionContrato version = guardarNuevaVersion(contrato, usuario, 1, contenido, null, null);

        auditoriaService.registrarEnContrato(usuario, "Creación", contrato.getIdContrato(), version.getIdVersion(),
                "Creación del contrato " + contrato.getNombre() + " (versión 1)", direccionIp, null,
                describirVersion(version));

        return toVersionGuardada(contrato, version, MENSAJE_CREADO);
    }

    // Postcondición CU-01: el contrato queda disponible en la lista del usuario creador
    @Transactional(readOnly = true)
    public List<ContratoResumenResponse> listarContratosDelUsuario(Usuario usuario) {
        List<ContratoResumenResponse> respuesta = new ArrayList<>();
        for (Contrato contrato : contratoRepository.findByUsuarioCreadorIdUsuario(usuario.getIdUsuario())) {
            ContratoResumenResponse resumen = new ContratoResumenResponse();
            resumen.setIdContrato(contrato.getIdContrato());
            resumen.setNombre(contrato.getNombre());
            resumen.setEstado(contrato.getEstado().getNombre());
            resumen.setResponsable(usuario.getNombre() + " " + usuario.getApellido());
            VersionContrato actual = versionActual(contrato.getIdContrato());
            resumen.setUltimaModificacion(actual.getFechaCreacion());
            respuesta.add(resumen);
        }
        return respuesta;
    }

    // CU-02 pasos 2 a 8: contrato con su versión actual e indicación de si es editable
    @Transactional(readOnly = true)
    public ContratoDetalleResponse obtenerContrato(Integer idContrato) {
        Contrato contrato = buscarContrato(idContrato);
        VersionContrato actual = versionActual(idContrato);

        ContratoDetalleResponse response = new ContratoDetalleResponse();
        response.setIdContrato(contrato.getIdContrato());
        response.setNombre(contrato.getNombre());
        response.setEstado(contrato.getEstado().getNombre());
        response.setPartesInvolucradas(contrato.getPartesInvolucradas());
        response.setFechaInicio(contrato.getFechaInicio());
        response.setFechaExpiracion(contrato.getFechaExpiracion());
        response.setDescripcionPropiedad(contrato.getDescripcionPropiedad());
        response.setIdPlantilla(contrato.getPlantilla() == null ? null : contrato.getPlantilla().getIdPlantilla());
        response.setNumeroVersion(actual.getNumeroVersion());
        response.setHash(actual.getHashSha256());
        response.setContenido(actual.getContenido());
        response.setFechaUltimaModificacion(actual.getFechaCreacion());
        response.setEditable(esEditable(contrato));
        return response;
    }

    /**
     * CU-02 pasos 13 a 20: guarda una nueva versión numerada con su hash.
     */
    @Transactional
    public VersionGuardadaResponse guardarVersion(Integer idContrato, GuardarVersionRequest request, Usuario usuario,
            String direccionIp) {
        Contrato contrato = buscarContrato(idContrato);

        // Paso 2 / camino alternativo: solo Borrador o En Revisión
        if (!esEditable(contrato)) {
            throw new ReglaNegocioException(MENSAJE_NO_EDITABLE);
        }

        // Pasos 14 y 15: cuerpo no vacío y con al menos 100 caracteres de texto (sin etiquetas HTML)
        if (ContenidoHtml.textoPlano(request.getContenido()).length() < CARACTERES_MINIMOS_CONTENIDO) {
            throw new ReglaNegocioException("contenido", MENSAJE_CONTENIDO_MINIMO);
        }

        // Pasos 16 a 19
        VersionContrato anterior = versionActual(idContrato);
        VersionContrato nueva = guardarNuevaVersion(contrato, usuario, anterior.getNumeroVersion() + 1,
                request.getContenido(), request.getComentario(), null);

        auditoriaService.registrarEnContrato(usuario, "Edición", contrato.getIdContrato(), nueva.getIdVersion(),
                "Nueva versión " + nueva.getNumeroVersion() + " del contrato " + contrato.getNombre(), direccionIp,
                describirVersion(anterior), describirVersion(nueva));

        // Paso 20: mensaje con el número de versión y el hash generado
        return toVersionGuardada(contrato, nueva, MENSAJE_VERSION_GUARDADA);
    }

    /**
     * Crea una versión nueva sin modificar las anteriores (CU-01, CU-02 y CU-14).
     */
    VersionContrato guardarNuevaVersion(Contrato contrato, Usuario autor, int numero, String contenido,
            String comentario, String razonRestauracion) {
        VersionContrato version = new VersionContrato();
        version.setContrato(contrato);
        version.setUsuarioAutor(autor);
        version.setNumeroVersion(numero);
        version.setContenido(contenido);
        version.setComentario(tieneTexto(comentario) ? comentario.trim() : null);
        version.setRazonRestauracion(tieneTexto(razonRestauracion) ? razonRestauracion.trim() : null);
        version.setFechaCreacion(LocalDateTime.now());
        version.setHashSha256(Sha256.calcular(contenido));
        return versionContratoRepository.save(version);
    }

    Contrato buscarContrato(Integer idContrato) {
        return contratoRepository.findById(idContrato)
                .orElseThrow(() -> new RecursoNoEncontradoException("Contrato no encontrado"));
    }

    VersionContrato versionActual(Integer idContrato) {
        return versionContratoRepository.findTopByContratoIdContratoOrderByNumeroVersionDesc(idContrato)
                .orElseThrow(() -> new RecursoNoEncontradoException("El contrato no tiene versiones"));
    }

    boolean esEditable(Contrato contrato) {
        String estado = contrato.getEstado().getNombre();
        return EstadoContrato.BORRADOR.equals(estado) || EstadoContrato.EN_REVISION.equals(estado);
    }

    /**
     * CU-01 paso 17: cuerpo de la plantilla con los marcadores del formulario reemplazados.
     * Los demás marcadores quedan tal cual para completarlos en el editor (CU-02).
     */
    String generarContenidoInicial(String cuerpoPlantilla, Contrato contrato) {
        Map<String, String> valores = new HashMap<>();
        valores.put("nombre_contrato", contrato.getNombre());
        valores.put("partes_involucradas", contrato.getPartesInvolucradas());
        valores.put("fecha_inicio", Fechas.formatear(contrato.getFechaInicio()));
        valores.put("fecha_expiracion", Fechas.formatear(contrato.getFechaExpiracion()));
        valores.put("descripcion_propiedad", contrato.getDescripcionPropiedad());

        Matcher matcher = PlantillaService.PATRON_MARCADOR.matcher(cuerpoPlantilla == null ? "" : cuerpoPlantilla);
        StringBuffer resultado = new StringBuffer();
        while (matcher.find()) {
            String marcador = matcher.group(1).trim();
            String reemplazo = matcher.group(0);
            if (valores.containsKey(marcador)) {
                // Los datos del formulario se insertan en contenido HTML
                reemplazo = ContenidoHtml.escapar(valores.get(marcador));
            }
            matcher.appendReplacement(resultado, Matcher.quoteReplacement(reemplazo));
        }
        matcher.appendTail(resultado);
        return resultado.toString();
    }

    private EstadoContrato buscarEstado(String nombre) {
        return estadoContratoRepository.findByNombre(nombre)
                .orElseThrow(() -> new IllegalStateException("No existe el estado de contrato " + nombre));
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }

    private String describirVersion(VersionContrato version) {
        return "Versión " + version.getNumeroVersion() + " | Hash: " + version.getHashSha256();
    }

    private VersionGuardadaResponse toVersionGuardada(Contrato contrato, VersionContrato version, String mensaje) {
        VersionGuardadaResponse response = new VersionGuardadaResponse();
        response.setIdContrato(contrato.getIdContrato());
        response.setEstado(contrato.getEstado().getNombre());
        response.setNumeroVersion(version.getNumeroVersion());
        response.setHash(version.getHashSha256());
        response.setMensaje(mensaje);
        return response;
    }
}
