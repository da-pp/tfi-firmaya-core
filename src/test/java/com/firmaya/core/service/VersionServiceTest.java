package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.firmaya.core.dto.CompararVersionesResponse;
import com.firmaya.core.dto.FragmentoComparacion;
import com.firmaya.core.dto.HistorialVersionesResponse;
import com.firmaya.core.dto.VerificarIntegridadResponse;
import com.firmaya.core.dto.VersionGuardadaResponse;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.VersionContrato;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.VersionContratoRepository;

class VersionServiceTest {

    private ContratoService contratoService;
    private VersionContratoRepository versionContratoRepository;
    private AuditoriaService auditoriaService;
    private VersionService service;
    private Usuario usuario = new Usuario();
    private Contrato contrato;
    private final List<VersionContrato> versiones = new ArrayList<>();

    @BeforeEach
    void preparar() {
        contratoService = mock(ContratoService.class);
        versionContratoRepository = mock(VersionContratoRepository.class);
        auditoriaService = mock(AuditoriaService.class);
        service = new VersionService(contratoService, versionContratoRepository, auditoriaService);

        contrato = new Contrato();
        contrato.setIdContrato(10);
        contrato.setEstado(estado(EstadoContrato.EN_REVISION));
        when(contratoService.buscarContrato(10)).thenReturn(contrato);
    }

    // ---------- Comparador de texto (CU-12) ----------

    @Test
    void comparadorDetectaUnCambioEnUnaPalabra() {
        List<FragmentoComparacion> fragmentos = ComparadorTexto.comparar(
                "El contrato tendrá una duración de 12 meses.", "El contrato tendrá una duración de 24 meses.");

        assertEquals(1, ComparadorTexto.contarCambios(fragmentos));
        assertEquals(FragmentoComparacion.ELIMINADO, fragmentos.get(1).getTipo());
        assertEquals("12", fragmentos.get(1).getTexto());
        assertEquals(FragmentoComparacion.AGREGADO, fragmentos.get(2).getTipo());
        assertEquals("24", fragmentos.get(2).getTexto());
    }

    @Test
    void comparadorCuentaBloquesSeparadosYTextosIguales() {
        List<FragmentoComparacion> dos = ComparadorTexto.comparar("uno dos tres cuatro", "UNO dos tres CUATRO");
        assertEquals(2, ComparadorTexto.contarCambios(dos));

        List<FragmentoComparacion> iguales = ComparadorTexto.comparar("mismo texto", "mismo texto");
        assertEquals(0, ComparadorTexto.contarCambios(iguales));
    }

    @Test
    void textoPlanoQuitaEtiquetasYConservaParrafos() {
        assertEquals("Hola\nMundo & <cia>", ContenidoHtml.textoPlano("<p>Hola</p><p><b>Mundo</b> &amp; &lt;cia&gt;</p>"));
    }

    // ---------- CU-11 ----------

    @Test
    void historialConUnaSolaVersionMuestraLeyenda() {
        agregarVersion(1, "<p>v1</p>");
        when(versionContratoRepository.findByContratoIdContratoOrderByNumeroVersionDesc(10)).thenReturn(versiones);

        HistorialVersionesResponse response = service.obtenerHistorial(10);

        assertEquals(VersionService.MENSAJE_SIN_VERSIONES_ANTERIORES, response.getMensaje());
        assertTrue(response.getVersiones().get(0).isActual());
        assertTrue(response.isRestauracionDisponible());
    }

    @Test
    void historialDeContratoFirmadoNoPermiteRestaurar() {
        contrato.setEstado(estado(EstadoContrato.FIRMADO));
        when(versionContratoRepository.findByContratoIdContratoOrderByNumeroVersionDesc(10))
                .thenReturn(Collections.<VersionContrato>emptyList());

        HistorialVersionesResponse response = service.obtenerHistorial(10);

        assertFalse(response.isRestauracionDisponible());
        assertEquals(VersionService.MENSAJE_RESTAURACION_NO_DISPONIBLE, response.getMensajeRestauracion());
    }

    // ---------- CU-12 ----------

    @Test
    void compararMismaVersionEsRechazado() {
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.compararVersiones(10, 2, 2));
        assertEquals(VersionService.MENSAJE_MISMA_VERSION, ex.getMessage());
    }

    @Test
    void compararVersionesSinDiferencias() {
        VersionContrato v1 = agregarVersion(1, "<p>Igual</p>");
        VersionContrato v2 = agregarVersion(2, "<p>Igual</p>");
        when(versionContratoRepository.findByContratoIdContratoAndNumeroVersion(10, 1)).thenReturn(Optional.of(v1));
        when(versionContratoRepository.findByContratoIdContratoAndNumeroVersion(10, 2)).thenReturn(Optional.of(v2));
        when(contratoService.versionActual(10)).thenReturn(v2);

        CompararVersionesResponse response = service.compararVersiones(10, 1, 2);

        assertEquals(0, response.getCantidadCambios());
        assertEquals(VersionService.MENSAJE_SIN_DIFERENCIAS, response.getMensaje());
        assertEquals(Integer.valueOf(1), response.getVersionA().getNumeroVersion());
        assertTrue(response.getVersionB().isActual());
    }

    // ---------- CU-13 ----------

    @Test
    void verificarIntegridadCompararaConVersionActivaYAudita() {
        VersionContrato actual = agregarVersion(3, "<p>contenido</p>");
        when(contratoService.versionActual(10)).thenReturn(actual);

        VerificarIntegridadResponse ok = service.verificarIntegridad(10, actual.getHashSha256(), usuario, "10.0.0.1");
        assertTrue(ok.isCoincide());
        assertEquals(VersionService.MENSAJE_INTEGRIDAD_OK, ok.getMensaje());

        String otroHash = Sha256.calcular("otro");
        VerificarIntegridadResponse error = service.verificarIntegridad(10, otroHash, usuario, "10.0.0.1");
        assertFalse(error.isCoincide());
        assertEquals(VersionService.MENSAJE_INTEGRIDAD_ERROR, error.getMensaje());
        assertEquals(actual.getHashSha256(), error.getHashAlmacenado());
        assertEquals(otroHash, error.getHashIngresado());
    }

    // ---------- CU-14 ----------

    @Test
    void restaurarCreaNuevaVersionConComentario() {
        VersionContrato v2 = agregarVersion(2, "<p>contenido viejo</p>");
        VersionContrato v4 = agregarVersion(4, "<p>contenido nuevo</p>");
        when(versionContratoRepository.findByContratoIdContratoAndNumeroVersion(10, 2)).thenReturn(Optional.of(v2));
        when(contratoService.versionActual(10)).thenReturn(v4);
        VersionContrato v5 = version(5, "<p>contenido viejo</p>");
        when(contratoService.guardarNuevaVersion(contrato, usuario, 5, "<p>contenido viejo</p>",
                "Restauración de la versión 2", "Se revirtió la cláusula")).thenReturn(v5);

        VersionGuardadaResponse response = service.restaurarVersion(10, 2, "Se revirtió la cláusula", usuario,
                "10.0.0.1");

        assertEquals(5, response.getNumeroVersion());
        assertEquals(v2.getHashSha256(), response.getHash());
        assertEquals("La versión 2 fue restaurada exitosamente como la nueva versión 5.", response.getMensaje());
        verify(auditoriaService).registrarEnContrato(eq(usuario), eq("Restauración"), eq(10), any(), anyString(),
                eq("10.0.0.1"), anyString(), anyString());
    }

    @Test
    void restaurarEnContratoArchivadoEsRechazado() {
        contrato.setEstado(estado(EstadoContrato.ARCHIVADO));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.restaurarVersion(10, 1, null, usuario, "10.0.0.1"));

        assertEquals(VersionService.MENSAJE_RESTAURACION_NO_DISPONIBLE, ex.getMessage());
        verify(contratoService, never()).guardarNuevaVersion(any(), any(), anyInt(), anyString(), anyString(),
                isNull());
    }

    private VersionContrato agregarVersion(int numero, String contenido) {
        VersionContrato version = version(numero, contenido);
        versiones.add(0, version);
        return version;
    }

    private VersionContrato version(int numero, String contenido) {
        Usuario autor = new Usuario();
        autor.setNombre("Daniel");
        autor.setApellido("Pérez");
        VersionContrato version = new VersionContrato();
        version.setNumeroVersion(numero);
        version.setContenido(contenido);
        version.setHashSha256(Sha256.calcular(contenido));
        version.setUsuarioAutor(autor);
        return version;
    }

    private EstadoContrato estado(String nombre) {
        EstadoContrato estado = new EstadoContrato();
        estado.setNombre(nombre);
        return estado;
    }
}
