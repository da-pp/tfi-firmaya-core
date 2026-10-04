package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.firmaya.core.dto.AuditoriaDetalleResponse;
import com.firmaya.core.dto.AuditoriaPaginaResponse;
import com.firmaya.core.entity.Auditoria;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.VersionContrato;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.AuditoriaRepository;
import com.firmaya.core.repository.ContratoRepository;
import com.firmaya.core.repository.VersionContratoRepository;

class AuditoriaConsultaServiceTest {

    private AuditoriaRepository auditoriaRepository;
    private VersionContratoRepository versionContratoRepository;
    private AuditoriaConsultaService service;

    @BeforeEach
    void preparar() {
        auditoriaRepository = mock(AuditoriaRepository.class);
        versionContratoRepository = mock(VersionContratoRepository.class);
        service = new AuditoriaConsultaService(auditoriaRepository, mock(ContratoRepository.class),
                versionContratoRepository);
    }

    @SuppressWarnings("unchecked")
    @Test
    void buscarDevuelvePaginasDe50OrdenadasYTotal() {
        Auditoria registro = registro("Inicio de sesión", "Ingreso");
        when(auditoriaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Arrays.asList(registro), PageRequest.of(1, 50), 51));

        AuditoriaPaginaResponse response = service.buscar(null, null, null, null, null, 2);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(auditoriaRepository).findAll(any(Specification.class), captor.capture());
        assertEquals(1, captor.getValue().getPageNumber());
        assertEquals(50, captor.getValue().getPageSize());
        assertEquals(Sort.Direction.DESC, captor.getValue().getSort().getOrderFor("fechaHora").getDirection());

        assertEquals(51, response.getTotal());
        assertEquals("51 registros encontrados.", response.getMensaje());
        assertEquals("ana@mail.com", response.getRegistros().get(0).getUsuario());
    }

    @SuppressWarnings("unchecked")
    @Test
    void buscarSinResultadosDevuelveMensaje() {
        when(auditoriaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.<Auditoria>emptyList(), PageRequest.of(0, 50), 0));

        AuditoriaPaginaResponse response = service.buscar(null, null, "nadie", null, null, 1);

        assertEquals(AuditoriaConsultaService.MENSAJE_SIN_RESULTADOS, response.getMensaje());
        assertTrue(response.getRegistros().isEmpty());
    }

    @Test
    void paginaMenorAUnoEsRechazada() {
        assertThrows(ReglaNegocioException.class, () -> service.buscar(null, null, null, null, null, 0));
    }

    @Test
    void detalleIncluyeVersionYHash() {
        Auditoria registro = registro("Cambio de estado", "Borrador a En Revisión");
        registro.setDatosAntes("Borrador");
        registro.setDatosDespues("En Revisión");
        registro.setIdVersion(7);
        VersionContrato version = new VersionContrato();
        version.setNumeroVersion(3);
        version.setHashSha256("abc123");
        when(auditoriaRepository.findById(1)).thenReturn(Optional.of(registro));
        when(versionContratoRepository.findById(7)).thenReturn(Optional.of(version));

        AuditoriaDetalleResponse detalle = service.obtenerDetalle(1);

        assertEquals("Borrador", detalle.getDatosAntes());
        assertEquals("En Revisión", detalle.getDatosDespues());
        assertEquals(3, detalle.getNumeroVersion());
        assertEquals("abc123", detalle.getHashVersion());
    }

    @SuppressWarnings("unchecked")
    @Test
    void exportarCsvConEncabezadoFormatoDeFechaYComillasEscapadas() {
        Auditoria registro = registro("Edición", "Texto con \"comillas\", y coma");
        when(auditoriaRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(Arrays.asList(registro));

        String csv = service.exportarCsv(null, null, null, null, null);

        String[] lineas = csv.split("\n");
        assertEquals("Fecha y hora,Usuario,Tipo de acción,Entidad afectada,Descripción,Dirección IP", lineas[0]);
        assertEquals("\"15/05/2026 11:30:05\",\"ana@mail.com\",\"Edición\",\"contrato\","
                + "\"Texto con \"\"comillas\"\", y coma\",\"181.44.10.22\"", lineas[1]);
    }

    private Auditoria registro(String tipoAccion, String descripcion) {
        Usuario usuario = new Usuario();
        usuario.setEmail("ana@mail.com");

        Auditoria auditoria = new Auditoria();
        auditoria.setIdRegistro(1);
        auditoria.setUsuario(usuario);
        auditoria.setTipoAccion(tipoAccion);
        auditoria.setEntidadAfectada("contrato");
        auditoria.setIdEntidadAfectada(10);
        auditoria.setDescripcion(descripcion);
        auditoria.setDireccionIp("181.44.10.22");
        auditoria.setFechaHora(LocalDateTime.of(2026, 5, 15, 11, 30, 5));
        return auditoria;
    }
}
