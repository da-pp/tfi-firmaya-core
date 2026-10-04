package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import com.firmaya.core.dto.ActividadResponse;
import com.firmaya.core.dto.ContratosPaginaResponse;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.ContratoRepository;
import com.firmaya.core.repository.EstadoContratoRepository;
import com.firmaya.core.repository.VersionContratoRepository;

class ActividadServiceTest {

    private ContratoRepository contratoRepository;
    private VersionContratoRepository versionContratoRepository;
    private EstadoContratoRepository estadoContratoRepository;
    private ActividadService service;

    private final List<Contrato> contratos = new ArrayList<>();
    private final List<Object[]> modificaciones = new ArrayList<>();
    private final List<Object[]> firmas = new ArrayList<>();
    private final LocalDateTime ahora = LocalDateTime.now();

    @BeforeEach
    void preparar() {
        contratoRepository = mock(ContratoRepository.class);
        versionContratoRepository = mock(VersionContratoRepository.class);
        estadoContratoRepository = mock(EstadoContratoRepository.class);
        service = new ActividadService(contratoRepository, versionContratoRepository, estadoContratoRepository);

        when(contratoRepository.findAll()).thenReturn(contratos);
        when(versionContratoRepository.buscarUltimaModificacionPorContrato()).thenReturn(modificaciones);
        when(contratoRepository.buscarUltimaFirmaPorContrato()).thenReturn(firmas);

        List<EstadoContrato> estados = new ArrayList<>();
        for (String nombre : Arrays.asList(EstadoContrato.BORRADOR, EstadoContrato.EN_REVISION,
                EstadoContrato.LISTO_PARA_FIRMAR, EstadoContrato.FIRMADO, EstadoContrato.ARCHIVADO)) {
            estados.add(estado(nombre));
        }
        when(estadoContratoRepository.findAll(any(Sort.class))).thenReturn(estados);
    }

    @Test
    void metricasDelPeriodoPredeterminado() {
        agregar(1, EstadoContrato.BORRADOR, ahora.minusDays(2), null, LocalDate.now().plusDays(3));
        agregar(2, EstadoContrato.LISTO_PARA_FIRMAR, ahora.minusDays(1), null, null);
        agregar(3, EstadoContrato.ARCHIVADO, ahora.minusDays(5), ahora.minusDays(10), null);
        agregar(4, EstadoContrato.FIRMADO, ahora.minusDays(3), ahora.minusDays(3), null);
        // Modificado hace 40 días: fuera de los últimos 30 días
        agregar(5, EstadoContrato.BORRADOR, ahora.minusDays(40), null, null);

        ActividadResponse response = service.obtenerActividad(null, null, null);

        assertEquals(LocalDate.now().minusDays(30), response.getFechaInicio());
        assertEquals(3, response.getContratosActivos());
        assertEquals(1, response.getContratosPendientesFirma());
        assertEquals(2, response.getContratosFirmados());
        assertEquals(1, response.getContratosProximosAVencer());
        assertEquals(1, response.getContratosPorEstado().get(0).getCantidad());
        assertEquals(5, response.getContratosPorEstado().size());
        // Los recientes van del más reciente al más antiguo
        assertEquals(Integer.valueOf(2), response.getContratosRecientes().get(0).getIdContrato());
        assertEquals("Ana Pérez", response.getContratosRecientes().get(0).getResponsable());
        assertNull(response.getMensaje());
    }

    @Test
    void filtroPorEstado() {
        agregar(1, EstadoContrato.BORRADOR, ahora.minusDays(2), null, null);
        agregar(2, EstadoContrato.LISTO_PARA_FIRMAR, ahora.minusDays(1), null, null);

        ActividadResponse response = service.obtenerActividad(null, null,
                Collections.singletonList(EstadoContrato.BORRADOR));

        assertEquals(1, response.getContratosActivos());
        assertEquals(0, response.getContratosPendientesFirma());
    }

    @Test
    void sinActividadDevuelveCerosYMensaje() {
        ActividadResponse response = service.obtenerActividad(null, null, null);

        assertEquals(0, response.getContratosActivos());
        assertEquals(ActividadService.MENSAJE_SIN_ACTIVIDAD, response.getMensaje());
    }

    @Test
    void rangoDeFechasInvalido() {
        LocalDate hoy = LocalDate.now();

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.obtenerActividad(hoy, hoy.minusDays(1), null));
        assertEquals(ActividadService.MENSAJE_RANGO_INVALIDO, ex.getMessage());

        assertThrows(ReglaNegocioException.class, () -> service.obtenerActividad(hoy, null, null));
    }

    @Test
    void detalleDeMetricaPaginaDe20() {
        for (int i = 1; i <= 25; i++) {
            agregar(i, EstadoContrato.BORRADOR, ahora.minusHours(i), null, null);
        }

        ContratosPaginaResponse pagina2 = service.listarContratosDeMetrica(ActividadService.METRICA_ACTIVOS,
                null, null, null, 2);

        assertEquals(25, pagina2.getTotal());
        assertEquals(2, pagina2.getTotalPaginas());
        assertEquals(5, pagina2.getContratos().size());
        assertThrows(ReglaNegocioException.class,
                () -> service.listarContratosDeMetrica("OTRA", null, null, null, 1));
    }

    @Test
    void exportarCsvDeLaMetrica() {
        agregar(1, EstadoContrato.LISTO_PARA_FIRMAR, LocalDateTime.of(LocalDate.now(), ahora.toLocalTime().withSecond(0).withNano(0)), null, null);

        String csv = service.exportarCsv(ActividadService.METRICA_PENDIENTES_FIRMA, null, null, null);

        String[] lineas = csv.split("\n");
        assertEquals("Nombre,Estado,Responsable,Última modificación", lineas[0]);
        assertTrue(lineas[1].startsWith("\"Contrato 1\",\"Listo para firmar\",\"Ana Pérez\","));
    }

    private void agregar(int id, String estado, LocalDateTime ultimaModificacion, LocalDateTime ultimaFirma,
            LocalDate vencimiento) {
        Usuario creador = new Usuario();
        creador.setNombre("Ana");
        creador.setApellido("Pérez");

        Contrato contrato = new Contrato();
        contrato.setIdContrato(id);
        contrato.setNombre("Contrato " + id);
        contrato.setEstado(estado(estado));
        contrato.setUsuarioCreador(creador);
        contrato.setFechaExpiracion(vencimiento);
        contratos.add(contrato);

        modificaciones.add(new Object[] { id, ultimaModificacion });
        if (ultimaFirma != null) {
            // La consulta nativa devuelve java.sql.Timestamp
            firmas.add(new Object[] { id, Timestamp.valueOf(ultimaFirma) });
        }
    }

    private EstadoContrato estado(String nombre) {
        EstadoContrato estado = new EstadoContrato();
        estado.setNombre(nombre);
        return estado;
    }
}
