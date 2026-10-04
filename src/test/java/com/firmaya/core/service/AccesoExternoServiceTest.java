package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.firmaya.core.dto.AccesoExternoResponse;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.UsuarioContrato;
import com.firmaya.core.entity.VersionContrato;
import com.firmaya.core.exception.NoAutenticadoException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.UsuarioContratoRepository;

class AccesoExternoServiceTest {

    private static final String TOKEN = "token-invitacion";

    private UsuarioContratoRepository usuarioContratoRepository;
    private ContratoService contratoService;
    private AuditoriaService auditoriaService;
    private AccesoExternoService service;
    private UsuarioContrato parte;

    @BeforeEach
    void preparar() {
        usuarioContratoRepository = mock(UsuarioContratoRepository.class);
        contratoService = mock(ContratoService.class);
        auditoriaService = mock(AuditoriaService.class);
        service = new AccesoExternoService(usuarioContratoRepository, contratoService, auditoriaService);

        EstadoContrato estado = new EstadoContrato();
        estado.setNombre(EstadoContrato.LISTO_PARA_FIRMAR);
        Contrato contrato = new Contrato();
        contrato.setIdContrato(10);
        contrato.setNombre("Locación Corrientes");
        contrato.setEstado(estado);

        parte = new UsuarioContrato();
        parte.setContrato(contrato);
        parte.setNombreParte("Ana Martínez");
        parte.setCorreoInvitado("ana@mail.com");
        parte.setRolParte(UsuarioContrato.ROL_FIRMANTE);
        parte.setFechaExpiracionToken(LocalDateTime.now().plusMinutes(10));
        when(usuarioContratoRepository.findByTokenInvitacion(TOKEN)).thenReturn(Optional.of(parte));

        VersionContrato actual = new VersionContrato();
        actual.setIdVersion(7);
        actual.setNumeroVersion(3);
        actual.setHashSha256("abc");
        actual.setContenido("<p>Contenido</p>");
        when(contratoService.versionActual(10)).thenReturn(actual);
    }

    @Test
    void accesoValidoRegistraAccesoYMuestraBotonFirmar() {
        AccesoExternoResponse response = service.verContrato(TOKEN, "181.44.10.22");

        assertEquals("Locación Corrientes", response.getNombreContrato());
        assertEquals(3, response.getNumeroVersion());
        assertEquals(UsuarioContrato.ROL_FIRMANTE, response.getRolParte());
        assertTrue(response.isPuedeFirmar());
        verify(auditoriaService).registrarEnContrato(isNull(), eq("Acceso externo"), eq(10), eq(7), anyString(),
                eq("181.44.10.22"), isNull(), isNull());
    }

    @Test
    void revisorNoVeBotonFirmar() {
        parte.setRolParte(UsuarioContrato.ROL_REVISOR);

        assertFalse(service.verContrato(TOKEN, "1.1.1.1").isPuedeFirmar());
    }

    @Test
    void tokenInexistenteOExpiradoEsRechazado() {
        when(usuarioContratoRepository.findByTokenInvitacion("otro")).thenReturn(Optional.empty());
        NoAutenticadoException ex = assertThrows(NoAutenticadoException.class,
                () -> service.verContrato("otro", "1.1.1.1"));
        assertEquals(AccesoExternoService.MENSAJE_TOKEN_INVALIDO, ex.getMessage());

        parte.setFechaExpiracionToken(LocalDateTime.now().minusMinutes(1));
        assertThrows(NoAutenticadoException.class, () -> service.verContrato(TOKEN, "1.1.1.1"));
    }

    @Test
    void contratoArchivadoNoEstaDisponible() {
        parte.getContrato().getEstado().setNombre(EstadoContrato.ARCHIVADO);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.verContrato(TOKEN, "1.1.1.1"));
        assertEquals(AccesoExternoService.MENSAJE_NO_DISPONIBLE, ex.getMessage());
    }

    @Test
    void renovarExtiendeElVencimiento() {
        AccesoExternoResponse response = service.renovarToken(TOKEN);

        assertTrue(response.getFechaExpiracionToken().isAfter(LocalDateTime.now().plusMinutes(14)));
        verify(usuarioContratoRepository).save(any(UsuarioContrato.class));
    }
}
