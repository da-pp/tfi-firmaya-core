package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.firmaya.core.dto.PlantillaRequest;
import com.firmaya.core.dto.PlantillaResponse;
import com.firmaya.core.entity.CampoPlantilla;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.Plantilla;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.exception.ConfirmacionRequeridaException;
import com.firmaya.core.repository.CampoPlantillaRepository;
import com.firmaya.core.repository.ContratoRepository;
import com.firmaya.core.repository.PlantillaRepository;

class PlantillaServiceTest {

    private PlantillaRepository plantillaRepository;
    private CampoPlantillaRepository campoPlantillaRepository;
    private ContratoRepository contratoRepository;
    private AuditoriaService auditoriaService;
    private PlantillaService service;
    private Usuario administrador = new Usuario();

    @BeforeEach
    void preparar() {
        plantillaRepository = mock(PlantillaRepository.class);
        campoPlantillaRepository = mock(CampoPlantillaRepository.class);
        contratoRepository = mock(ContratoRepository.class);
        auditoriaService = mock(AuditoriaService.class);
        service = new PlantillaService(plantillaRepository, campoPlantillaRepository, contratoRepository,
                auditoriaService);
    }

    @Test
    void extraeMarcadoresSinRepetirEnOrden() {
        List<String> marcadores = PlantillaService.extraerMarcadores(
                "<p>Entre {{locador}} y {{ locatario }}, {{locador}} declara...</p>");

        assertEquals(Arrays.asList("locador", "locatario"), marcadores);
    }

    @SuppressWarnings("unchecked")
    @Test
    void crearGuardaVersion1YCamposDeTipoTexto() {
        PlantillaResponse response = service.crearPlantilla(request("Entre {{locador}} y {{locatario}}", false),
                administrador, "10.0.0.1");

        assertEquals(1, response.getVersion());
        assertEquals(PlantillaService.MENSAJE_GUARDADA, response.getMensaje());
        ArgumentCaptor<List<CampoPlantilla>> captor = ArgumentCaptor.forClass(List.class);
        verify(campoPlantillaRepository).saveAll(captor.capture());
        assertEquals(2, captor.getValue().size());
        assertEquals(CampoPlantilla.TIPO_TEXTO, captor.getValue().get(0).getTipoDato());
        verify(auditoriaService).registrar(eq(administrador), eq("Creación"), eq("plantilla"), any(), anyString(),
                eq("10.0.0.1"));
    }

    @Test
    void sinCamposDinamicosPideConfirmacion() {
        ConfirmacionRequeridaException ex = assertThrows(ConfirmacionRequeridaException.class,
                () -> service.crearPlantilla(request("Texto fijo", false), administrador, "10.0.0.1"));

        assertEquals(PlantillaService.MENSAJE_SIN_CAMPOS, ex.getMessage());
        verify(plantillaRepository, never()).save(any(Plantilla.class));

        // Con "Guardar sin campos" se guarda igual
        PlantillaResponse response = service.crearPlantilla(request("Texto fijo", true), administrador, "10.0.0.1");
        assertEquals(1, response.getVersion());
    }

    @Test
    void editarCreaNuevaVersionDeLaPlantilla() {
        Plantilla existente = new Plantilla();
        existente.setIdPlantilla(3);
        existente.setVersion(2);
        when(plantillaRepository.findById(3)).thenReturn(Optional.of(existente));

        PlantillaResponse response = service.editarPlantilla(3, request("Nuevo {{campo}}", false), administrador,
                "10.0.0.1");

        assertEquals(3, response.getVersion());
        verify(campoPlantillaRepository).deleteByPlantillaIdPlantilla(3);
    }

    @Test
    void detalleIndicaContratosActivos() {
        Plantilla existente = new Plantilla();
        existente.setIdPlantilla(3);
        existente.setCuerpo("{{a}}");
        when(plantillaRepository.findById(3)).thenReturn(Optional.of(existente));
        when(contratoRepository.existsByPlantillaIdPlantillaAndEstadoNombreNot(3, EstadoContrato.ARCHIVADO))
                .thenReturn(true);

        assertEquals(Boolean.TRUE, service.obtenerPlantilla(3).getTieneContratosActivos());
    }

    private PlantillaRequest request(String cuerpo, boolean guardarSinCampos) {
        PlantillaRequest request = new PlantillaRequest();
        request.setNombre("Locación vivienda");
        request.setTipoContrato("Arrendamiento");
        request.setCuerpo(cuerpo);
        request.setEstado("Activa");
        request.setGuardarSinCampos(guardarSinCampos);
        return request;
    }
}
