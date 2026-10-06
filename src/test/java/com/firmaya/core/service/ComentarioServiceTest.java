package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.firmaya.core.dto.ComentarioRequest;
import com.firmaya.core.dto.ComentariosResponse;
import com.firmaya.core.entity.Comentario;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.PreferenciaNotificacion;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.UsuarioContrato;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.ComentarioRepository;
import com.firmaya.core.repository.UsuarioContratoRepository;

class ComentarioServiceTest {

    private ComentarioRepository comentarioRepository;
    private UsuarioContratoRepository usuarioContratoRepository;
    private ContratoService contratoService;
    private AccesoExternoService accesoExternoService;
    private NotificacionService notificacionService;
    private ComentarioService service;
    private Usuario creador;
    private Contrato contrato;
    private UsuarioContrato revisor;
    private UsuarioContrato firmante;

    @BeforeEach
    void preparar() {
        comentarioRepository = mock(ComentarioRepository.class);
        usuarioContratoRepository = mock(UsuarioContratoRepository.class);
        contratoService = mock(ContratoService.class);
        accesoExternoService = mock(AccesoExternoService.class);
        notificacionService = mock(NotificacionService.class);
        service = new ComentarioService(comentarioRepository, usuarioContratoRepository, contratoService,
                accesoExternoService, notificacionService, mock(AuditoriaService.class));

        creador = new Usuario();
        creador.setIdUsuario(1);
        creador.setNombre("María");
        creador.setApellido("Gómez");
        creador.setEmail("maria@mail.com");

        EstadoContrato estado = new EstadoContrato();
        estado.setNombre(EstadoContrato.EN_REVISION);
        contrato = new Contrato();
        contrato.setIdContrato(10);
        contrato.setNombre("Locación");
        contrato.setEstado(estado);
        contrato.setUsuarioCreador(creador);
        when(contratoService.buscarContrato(10)).thenReturn(contrato);

        revisor = parte(5, "carlos@mail.com", UsuarioContrato.ROL_REVISOR);
        firmante = parte(6, "ana@mail.com", UsuarioContrato.ROL_FIRMANTE);
        when(usuarioContratoRepository.findByContratoIdContratoOrderByIdParte(10))
                .thenReturn(Arrays.asList(revisor, firmante));
        when(comentarioRepository.countByContratoIdContrato(10)).thenReturn(4L);
    }

    @Test
    void comentarioInternoSeRegistraYNotificaALasPartes() {
        ComentariosResponse response = service.publicarComentario(10, request("Revisar el plazo", "plazo"), creador,
                "10.0.0.1");

        assertEquals("María Gómez", response.getComentario().getAutor());
        assertEquals("plazo", response.getComentario().getTextoSeleccionado());
        assertEquals(4, response.getTotalComentarios());
        assertEquals(ComentarioService.MENSAJE_PUBLICADO, response.getMensaje());
        verify(comentarioRepository).save(any(Comentario.class));
        verify(notificacionService).notificarEvento(isNull(), eq("carlos@mail.com"), eq(10),
                eq(PreferenciaNotificacion.EVENTO_NUEVO_COMENTARIO), anyString(), anyString());
        verify(notificacionService).notificarEvento(isNull(), eq("ana@mail.com"), eq(10), anyString(), anyString(),
                anyString());
        verify(notificacionService, never()).notificarEvento(eq(creador), anyString(), any(), anyString(),
                anyString(), anyString());
    }

    @Test
    void comentarioExternoNotificaAlCreadorYALasOtrasPartes() {
        when(accesoExternoService.buscarParteConAcceso("tk")).thenReturn(revisor);

        ComentariosResponse response = service.publicarComentarioExterno("tk", request("Observación", null),
                "10.0.0.1");

        assertEquals("Revisor 5", response.getComentario().getAutor());
        verify(notificacionService).notificarEvento(eq(creador), eq("maria@mail.com"), eq(10), anyString(),
                anyString(), anyString());
        verify(notificacionService).notificarEvento(isNull(), eq("ana@mail.com"), eq(10), anyString(), anyString(),
                anyString());
        verify(notificacionService, never()).notificarEvento(any(), eq("carlos@mail.com"), any(), anyString(),
                anyString(), anyString());
    }

    @Test
    void parteSoloLecturaNoPuedeComentar() {
        UsuarioContrato lector = parte(7, "lector@mail.com", UsuarioContrato.ROL_SOLO_LECTURA);
        when(accesoExternoService.buscarParteConAcceso("tk")).thenReturn(lector);

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.publicarComentarioExterno("tk", request("Hola", null), "10.0.0.1"));

        assertEquals(ComentarioService.MENSAJE_ROL_SOLO_LECTURA, ex.getMessage());
        verify(comentarioRepository, never()).save(any(Comentario.class));
    }

    @Test
    void contratoArchivadoNoAdmiteComentarios() {
        contrato.getEstado().setNombre(EstadoContrato.ARCHIVADO);

        assertThrows(ReglaNegocioException.class,
                () -> service.publicarComentario(10, request("Hola", null), creador, "10.0.0.1"));
    }

    private UsuarioContrato parte(int id, String correo, String rol) {
        UsuarioContrato parte = new UsuarioContrato();
        parte.setIdParte(id);
        parte.setContrato(contrato);
        parte.setCorreoInvitado(correo);
        parte.setNombreParte(rol + " " + id);
        parte.setRolParte(rol);
        return parte;
    }

    private ComentarioRequest request(String texto, String textoSeleccionado) {
        ComentarioRequest request = new ComentarioRequest();
        request.setTexto(texto);
        request.setTextoSeleccionado(textoSeleccionado);
        return request;
    }
}
