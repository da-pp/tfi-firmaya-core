package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.UsuarioContrato;
import com.firmaya.core.exception.NoAutenticadoException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.UsuarioContratoRepository;

class AccesoExternoServiceTest {

    private static final String TOKEN = "token-invitacion";

    private UsuarioContratoRepository usuarioContratoRepository;
    private AccesoExternoService service;
    private UsuarioContrato parte;

    @BeforeEach
    void preparar() {
        usuarioContratoRepository = mock(UsuarioContratoRepository.class);
        service = new AccesoExternoService(usuarioContratoRepository);

        EstadoContrato estado = new EstadoContrato();
        estado.setNombre(EstadoContrato.LISTO_PARA_FIRMAR);
        Contrato contrato = new Contrato();
        contrato.setIdContrato(10);
        contrato.setEstado(estado);

        parte = new UsuarioContrato();
        parte.setContrato(contrato);
        parte.setRolParte(UsuarioContrato.ROL_FIRMANTE);
        parte.setFechaExpiracionToken(LocalDateTime.now().plusMinutes(10));
        when(usuarioContratoRepository.findByTokenInvitacion(TOKEN)).thenReturn(Optional.of(parte));
    }

    @Test
    void tokenVigenteDevuelveLaParte() {
        assertSame(parte, service.buscarParteConAcceso(TOKEN));
    }

    @Test
    void tokenInexistenteOExpiradoEsRechazado() {
        when(usuarioContratoRepository.findByTokenInvitacion("otro")).thenReturn(Optional.empty());
        NoAutenticadoException ex = assertThrows(NoAutenticadoException.class,
                () -> service.buscarParteConAcceso("otro"));
        assertEquals(AccesoExternoService.MENSAJE_TOKEN_INVALIDO, ex.getMessage());

        parte.setFechaExpiracionToken(LocalDateTime.now().minusMinutes(1));
        assertThrows(NoAutenticadoException.class, () -> service.buscarParteConAcceso(TOKEN));
    }

    @Test
    void contratoArchivadoNoEstaDisponible() {
        parte.getContrato().getEstado().setNombre(EstadoContrato.ARCHIVADO);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.buscarParteConAcceso(TOKEN));
        assertEquals(AccesoExternoService.MENSAJE_NO_DISPONIBLE, ex.getMessage());
    }
}
