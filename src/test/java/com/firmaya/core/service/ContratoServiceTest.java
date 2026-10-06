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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.firmaya.core.dto.CrearContratoRequest;
import com.firmaya.core.dto.GuardarVersionRequest;
import com.firmaya.core.dto.VersionGuardadaResponse;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.Plantilla;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.VersionContrato;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.ContratoRepository;
import com.firmaya.core.repository.EstadoContratoRepository;
import com.firmaya.core.repository.PlantillaRepository;
import com.firmaya.core.repository.VersionContratoRepository;

class ContratoServiceTest {

    private static final DateTimeFormatter DD_MM_AAAA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String CONTENIDO_VALIDO = "<p>" + repetir("Cláusula ", 15) + "</p>";

    private ContratoRepository contratoRepository;
    private VersionContratoRepository versionContratoRepository;
    private PlantillaRepository plantillaRepository;
    private EstadoContratoRepository estadoContratoRepository;
    private AuditoriaService auditoriaService;
    private ContratoService service;
    private Usuario usuario = new Usuario();
    private Plantilla plantilla;

    @BeforeEach
    void preparar() {
        contratoRepository = mock(ContratoRepository.class);
        versionContratoRepository = mock(VersionContratoRepository.class);
        plantillaRepository = mock(PlantillaRepository.class);
        estadoContratoRepository = mock(EstadoContratoRepository.class);
        auditoriaService = mock(AuditoriaService.class);
        service = new ContratoService(contratoRepository, versionContratoRepository, plantillaRepository,
                estadoContratoRepository, auditoriaService);

        plantilla = new Plantilla();
        plantilla.setIdPlantilla(1);
        plantilla.setEstado(Plantilla.ESTADO_ACTIVA);
        plantilla.setCuerpo("<p>Contrato {{nombre_contrato}} entre {{partes_involucradas}} desde {{fecha_inicio}}. "
                + "Garante: {{garante}}</p>");
        when(plantillaRepository.findById(1)).thenReturn(Optional.of(plantilla));
        when(estadoContratoRepository.findByNombre(EstadoContrato.BORRADOR))
                .thenReturn(Optional.of(estado(EstadoContrato.BORRADOR)));
        when(versionContratoRepository.save(any(VersionContrato.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
    }

    @Test
    void sha256EsHexadecimalDe64Caracteres() {
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", Sha256.calcular("abc"));
    }

    @Test
    void crearContratoGeneraVersion1EnBorradorConHash() {
        CrearContratoRequest request = request(LocalDate.now().format(DD_MM_AAAA));
        request.setPartesInvolucradas("Juan <Pérez> & Ana");

        VersionGuardadaResponse response = service.crearContrato(request, usuario, "10.0.0.1");

        ArgumentCaptor<VersionContrato> captor = ArgumentCaptor.forClass(VersionContrato.class);
        verify(versionContratoRepository).save(captor.capture());
        VersionContrato version = captor.getValue();
        String esperado = "<p>Contrato Locación Corrientes entre Juan &lt;Pérez&gt; &amp; Ana desde "
                + LocalDate.now().format(DD_MM_AAAA) + ". Garante: {{garante}}</p>";
        assertEquals(esperado, version.getContenido());
        assertEquals(1, version.getNumeroVersion());
        assertEquals(Sha256.calcular(esperado), response.getHash());
        assertEquals(EstadoContrato.BORRADOR, response.getEstado());
        assertEquals(ContratoService.MENSAJE_CREADO, response.getMensaje());
        verify(auditoriaService).registrarEnContrato(eq(usuario), eq("Creación"), any(), any(), anyString(),
                eq("10.0.0.1"), any(), anyString());
    }

    @Test
    void fechaDeInicioAnteriorAHoyOInexistenteEsRechazada() {
        ReglaNegocioException pasada = assertThrows(ReglaNegocioException.class, () -> service
                .crearContrato(request(LocalDate.now().minusDays(1).format(DD_MM_AAAA)), usuario, "10.0.0.1"));
        assertEquals("fechaInicio", pasada.getCampo());
        assertEquals(CrearContratoRequest.MENSAJE_FECHA_INICIO, pasada.getMessage());

        assertThrows(ReglaNegocioException.class,
                () -> service.crearContrato(request("31/02/2099"), usuario, "10.0.0.1"));
        verify(contratoRepository, never()).save(any(Contrato.class));
    }

    @Test
    void plantillaInactivaEsRechazada() {
        plantilla.setEstado(Plantilla.ESTADO_INACTIVA);

        assertThrows(ReglaNegocioException.class,
                () -> service.crearContrato(request(LocalDate.now().format(DD_MM_AAAA)), usuario, "10.0.0.1"));
    }

    @Test
    void guardarVersionIncrementaNumeroYCalculaHash() {
        prepararContrato(EstadoContrato.EN_REVISION, 3);

        VersionGuardadaResponse response = service.guardarVersion(10, versionRequest(CONTENIDO_VALIDO), usuario,
                "10.0.0.1");

        assertEquals(4, response.getNumeroVersion());
        assertEquals(Sha256.calcular(CONTENIDO_VALIDO), response.getHash());
        assertEquals(ContratoService.MENSAJE_VERSION_GUARDADA, response.getMensaje());
    }

    @Test
    void contratoFirmadoNoEsEditable() {
        prepararContrato(EstadoContrato.FIRMADO, 3);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.guardarVersion(10, versionRequest(CONTENIDO_VALIDO), usuario, "10.0.0.1"));

        assertEquals(ContratoService.MENSAJE_NO_EDITABLE, ex.getMessage());
    }

    @Test
    void contenidoConMenosDe100CaracteresDeTextoEsRechazado() {
        prepararContrato(EstadoContrato.BORRADOR, 1);
        String corto = "<p><strong>" + repetir("a", 99) + "</strong></p>";

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.guardarVersion(10, versionRequest(corto), usuario, "10.0.0.1"));

        assertEquals(ContratoService.MENSAJE_CONTENIDO_MINIMO, ex.getMessage());
    }

    private void prepararContrato(String nombreEstado, int versionActual) {
        Contrato contrato = new Contrato();
        contrato.setIdContrato(10);
        contrato.setNombre("Locación");
        contrato.setEstado(estado(nombreEstado));
        when(contratoRepository.findById(10)).thenReturn(Optional.of(contrato));

        VersionContrato actual = new VersionContrato();
        actual.setNumeroVersion(versionActual);
        actual.setHashSha256("hash-anterior");
        when(versionContratoRepository.findTopByContratoIdContratoOrderByNumeroVersionDesc(10))
                .thenReturn(Optional.of(actual));
    }

    private CrearContratoRequest request(String fechaInicio) {
        CrearContratoRequest request = new CrearContratoRequest();
        request.setIdPlantilla(1);
        request.setNombre("Locación Corrientes");
        request.setPartesInvolucradas("Juan Pérez / Ana Martínez");
        request.setFechaInicio(fechaInicio);
        return request;
    }

    private GuardarVersionRequest versionRequest(String contenido) {
        GuardarVersionRequest request = new GuardarVersionRequest();
        request.setContenido(contenido);
        request.setComentario("Ajuste de cláusula");
        return request;
    }

    private EstadoContrato estado(String nombre) {
        EstadoContrato estado = new EstadoContrato();
        estado.setNombre(nombre);
        return estado;
    }

    private static String repetir(String texto, int veces) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < veces; i++) {
            sb.append(texto);
        }
        return sb.toString();
    }
}
