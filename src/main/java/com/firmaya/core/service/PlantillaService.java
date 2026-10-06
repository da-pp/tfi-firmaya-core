package com.firmaya.core.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.PlantillaRequest;
import com.firmaya.core.dto.PlantillaResponse;
import com.firmaya.core.entity.CampoPlantilla;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.Plantilla;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.exception.ConfirmacionRequeridaException;
import com.firmaya.core.exception.RecursoNoEncontradoException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.CampoPlantillaRepository;
import com.firmaya.core.repository.ContratoRepository;
import com.firmaya.core.repository.PlantillaRepository;

@Service
public class PlantillaService {

    static final String MENSAJE_SIN_CAMPOS = "La plantilla no tiene campos dinámicos definidos. "
            + "¿Desea guardarla de todos modos?";
    static final String MENSAJE_GUARDADA = "Plantilla guardada exitosamente.";

    static final Pattern PATRON_MARCADOR = Pattern.compile("\\{\\{([^{}]+)\\}\\}");
    private static final int LONGITUD_MAXIMA_MARCADOR = 100;

    private final PlantillaRepository plantillaRepository;
    private final CampoPlantillaRepository campoPlantillaRepository;
    private final ContratoRepository contratoRepository;
    private final AuditoriaService auditoriaService;

    public PlantillaService(PlantillaRepository plantillaRepository,
            CampoPlantillaRepository campoPlantillaRepository, ContratoRepository contratoRepository,
            AuditoriaService auditoriaService) {
        this.plantillaRepository = plantillaRepository;
        this.campoPlantillaRepository = campoPlantillaRepository;
        this.contratoRepository = contratoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<PlantillaResponse> listarPlantillas() {
        List<PlantillaResponse> respuesta = new ArrayList<>();
        for (Plantilla plantilla : plantillaRepository.findAll(Sort.by("nombre"))) {
            respuesta.add(toResumen(plantilla));
        }
        return respuesta;
    }

    @Transactional(readOnly = true)
    public PlantillaResponse obtenerPlantilla(Integer idPlantilla) {
        Plantilla plantilla = buscarPlantilla(idPlantilla);
        PlantillaResponse response = toDetalle(plantilla);
        response.setTieneContratosActivos(contratoRepository
                .existsByPlantillaIdPlantillaAndEstadoNombreNot(idPlantilla, EstadoContrato.ARCHIVADO));
        return response;
    }

    @Transactional(readOnly = true)
    public List<PlantillaResponse> listarPlantillasActivas() {
        List<PlantillaResponse> respuesta = new ArrayList<>();
        for (Plantilla plantilla : plantillaRepository.findByEstadoOrderByTipoContratoAscNombreAsc(
                Plantilla.ESTADO_ACTIVA)) {
            respuesta.add(toDetalle(plantilla));
        }
        return respuesta;
    }

    @Transactional
    public PlantillaResponse crearPlantilla(PlantillaRequest request, Usuario administrador, String direccionIp) {
        List<String> marcadores = validarMarcadores(request);

        Plantilla plantilla = new Plantilla();
        copiarDatos(request, plantilla);
        plantilla.setVersion(1);
        plantillaRepository.save(plantilla);
        guardarCampos(plantilla, marcadores);

        auditoriaService.registrar(administrador, "Creación", "plantilla", plantilla.getIdPlantilla(),
                "Creación de la plantilla " + plantilla.getNombre() + " (versión 1)", direccionIp);
        PlantillaResponse response = toDetalle(plantilla);
        response.setMensaje(MENSAJE_GUARDADA);
        return response;
    }

    @Transactional
    public PlantillaResponse editarPlantilla(Integer idPlantilla, PlantillaRequest request, Usuario administrador,
            String direccionIp) {
        Plantilla plantilla = buscarPlantilla(idPlantilla);
        List<String> marcadores = validarMarcadores(request);

        copiarDatos(request, plantilla);
        plantilla.setVersion(plantilla.getVersion() == null ? 1 : plantilla.getVersion() + 1);
        plantillaRepository.save(plantilla);

        campoPlantillaRepository.deleteByPlantillaIdPlantilla(idPlantilla);
        guardarCampos(plantilla, marcadores);

        auditoriaService.registrar(administrador, "Edición", "plantilla", plantilla.getIdPlantilla(),
                "Edición de la plantilla " + plantilla.getNombre() + " (versión " + plantilla.getVersion() + ")",
                direccionIp);
        PlantillaResponse response = toDetalle(plantilla);
        response.setMensaje(MENSAJE_GUARDADA);
        return response;
    }

    public static List<String> extraerMarcadores(String cuerpo) {
        Set<String> marcadores = new LinkedHashSet<>();
        Matcher matcher = PATRON_MARCADOR.matcher(cuerpo == null ? "" : cuerpo);
        while (matcher.find()) {
            String marcador = matcher.group(1).trim();
            if (!marcador.isEmpty()) {
                marcadores.add(marcador);
            }
        }
        return new ArrayList<>(marcadores);
    }

    private List<String> validarMarcadores(PlantillaRequest request) {
        List<String> marcadores = extraerMarcadores(request.getCuerpo());
        if (marcadores.isEmpty() && !request.isGuardarSinCampos()) {
            throw new ConfirmacionRequeridaException(MENSAJE_SIN_CAMPOS);
        }
        for (String marcador : marcadores) {
            if (marcador.length() > LONGITUD_MAXIMA_MARCADOR) {
                throw new ReglaNegocioException("cuerpo",
                        "El campo dinámico {{" + marcador + "}} no puede superar los 100 caracteres");
            }
        }
        return marcadores;
    }

    private void copiarDatos(PlantillaRequest request, Plantilla plantilla) {
        plantilla.setNombre(request.getNombre().trim());
        plantilla.setTipoContrato(request.getTipoContrato());
        plantilla.setDescripcionUso(request.getDescripcionUso());
        plantilla.setCuerpo(request.getCuerpo());
        plantilla.setEstado(request.getEstado());
    }

    private void guardarCampos(Plantilla plantilla, List<String> marcadores) {
        List<CampoPlantilla> campos = new ArrayList<>();
        for (String marcador : marcadores) {
            CampoPlantilla campo = new CampoPlantilla();
            campo.setPlantilla(plantilla);
            campo.setMarcador(marcador);
            campo.setTipoDato(CampoPlantilla.TIPO_TEXTO);
            campos.add(campo);
        }
        campoPlantillaRepository.saveAll(campos);
    }

    private Plantilla buscarPlantilla(Integer idPlantilla) {
        return plantillaRepository.findById(idPlantilla)
                .orElseThrow(() -> new RecursoNoEncontradoException("Plantilla no encontrada"));
    }

    private PlantillaResponse toResumen(Plantilla plantilla) {
        PlantillaResponse response = new PlantillaResponse();
        response.setIdPlantilla(plantilla.getIdPlantilla());
        response.setNombre(plantilla.getNombre());
        response.setTipoContrato(plantilla.getTipoContrato());
        response.setDescripcionUso(plantilla.getDescripcionUso());
        response.setEstado(plantilla.getEstado());
        response.setVersion(plantilla.getVersion());
        return response;
    }

    private PlantillaResponse toDetalle(Plantilla plantilla) {
        PlantillaResponse response = toResumen(plantilla);
        response.setCuerpo(plantilla.getCuerpo());
        response.setCamposDinamicos(extraerMarcadores(plantilla.getCuerpo()));
        return response;
    }
}
