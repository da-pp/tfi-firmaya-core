package com.firmaya.core.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.ComentarioRequest;
import com.firmaya.core.dto.ComentarioResponse;
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

@Service
public class ComentarioService {

    static final String MENSAJE_PUBLICADO = "Comentario publicado con éxito";
    static final String MENSAJE_ROL_SOLO_LECTURA = "No puede añadir comentarios con su rol actual.";
    static final String MENSAJE_ARCHIVADO = "No se pueden añadir comentarios a un contrato archivado.";

    private final ComentarioRepository comentarioRepository;
    private final UsuarioContratoRepository usuarioContratoRepository;
    private final ContratoService contratoService;
    private final AccesoExternoService accesoExternoService;
    private final NotificacionService notificacionService;
    private final AuditoriaService auditoriaService;

    public ComentarioService(ComentarioRepository comentarioRepository,
            UsuarioContratoRepository usuarioContratoRepository, ContratoService contratoService,
            AccesoExternoService accesoExternoService, NotificacionService notificacionService,
            AuditoriaService auditoriaService) {
        this.comentarioRepository = comentarioRepository;
        this.usuarioContratoRepository = usuarioContratoRepository;
        this.contratoService = contratoService;
        this.accesoExternoService = accesoExternoService;
        this.notificacionService = notificacionService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public ComentariosResponse listarComentarios(Integer idContrato) {
        contratoService.buscarContrato(idContrato);
        return listar(idContrato);
    }

    @Transactional(readOnly = true)
    public ComentariosResponse listarComentariosExterno(String token) {
        UsuarioContrato parte = accesoExternoService.buscarParteConAcceso(token);
        return listar(parte.getContrato().getIdContrato());
    }

    @Transactional
    public ComentariosResponse publicarComentario(Integer idContrato, ComentarioRequest request, Usuario usuario,
            String direccionIp) {
        Contrato contrato = contratoService.buscarContrato(idContrato);
        Comentario comentario = new Comentario();
        comentario.setUsuario(usuario);
        return publicar(contrato, comentario, request, null, usuario, direccionIp);
    }

    @Transactional
    public ComentariosResponse publicarComentarioExterno(String token, ComentarioRequest request,
            String direccionIp) {
        UsuarioContrato parte = accesoExternoService.buscarParteConAcceso(token);
        // El rol Solo lectura no puede comentar
        if (UsuarioContrato.ROL_SOLO_LECTURA.equals(parte.getRolParte())) {
            throw new ReglaNegocioException(MENSAJE_ROL_SOLO_LECTURA);
        }
        Comentario comentario = new Comentario();
        comentario.setParte(parte);
        comentario.setUsuario(parte.getUsuario());
        return publicar(parte.getContrato(), comentario, request, parte, parte.getUsuario(), direccionIp);
    }

    private ComentariosResponse publicar(Contrato contrato, Comentario comentario, ComentarioRequest request,
            UsuarioContrato parteAutora, Usuario usuarioAutor, String direccionIp) {
        if (EstadoContrato.ARCHIVADO.equals(contrato.getEstado().getNombre())) {
            throw new ReglaNegocioException(MENSAJE_ARCHIVADO);
        }

        String textoSeleccionado = request.getTextoSeleccionado();
        comentario.setContrato(contrato);
        comentario.setTexto(request.getTexto().trim());
        comentario.setTextoSeleccionado(
                textoSeleccionado == null || textoSeleccionado.trim().isEmpty() ? null : textoSeleccionado);
        comentario.setFechaPublicacion(LocalDateTime.now());
        comentarioRepository.save(comentario);

        String autor = nombreAutor(comentario);
        auditoriaService.registrarEnContrato(usuarioAutor, "Comentario", contrato.getIdContrato(), null,
                "Comentario de " + autor, direccionIp, null, null);

        notificarOtrasPartes(contrato, parteAutora, usuarioAutor, autor, comentario.getTexto());

        ComentariosResponse response = new ComentariosResponse();
        response.setComentario(toResponse(comentario));
        response.setTotalComentarios(comentarioRepository.countByContratoIdContrato(contrato.getIdContrato()));
        response.setMensaje(MENSAJE_PUBLICADO);
        return response;
    }

    private void notificarOtrasPartes(Contrato contrato, UsuarioContrato parteAutora, Usuario usuarioAutor,
            String autor, String texto) {
        String mensaje = autor + " publicó un comentario en el contrato \"" + contrato.getNombre() + "\":\n\n" + texto;
        String asunto = "FirmaYA - Nuevo comentario";
        String evento = PreferenciaNotificacion.EVENTO_NUEVO_COMENTARIO;
        Integer idContrato = contrato.getIdContrato();

        Usuario creador = contrato.getUsuarioCreador();
        if (creador != null && !esMismoUsuario(creador, usuarioAutor)) {
            notificacionService.notificarEvento(creador, creador.getEmail(), idContrato, evento, asunto, mensaje);
        }
        for (UsuarioContrato parte : usuarioContratoRepository.findByContratoIdContratoOrderByIdParte(idContrato)) {
            boolean esAutora = parteAutora != null && parte.getIdParte().equals(parteAutora.getIdParte());
            if (!esAutora && !esMismoUsuario(parte.getUsuario(), usuarioAutor)
                    && !esMismoUsuario(parte.getUsuario(), creador)) {
                notificacionService.notificarEvento(parte.getUsuario(), parte.getCorreoInvitado(), idContrato, evento,
                        asunto, mensaje);
            }
        }
    }

    private boolean esMismoUsuario(Usuario a, Usuario b) {
        return a != null && b != null && a.getIdUsuario() != null && a.getIdUsuario().equals(b.getIdUsuario());
    }

    private ComentariosResponse listar(Integer idContrato) {
        List<ComentarioResponse> comentarios = new ArrayList<>();
        for (Comentario comentario : comentarioRepository
                .findByContratoIdContratoOrderByFechaPublicacionAscIdComentarioAsc(idContrato)) {
            comentarios.add(toResponse(comentario));
        }
        ComentariosResponse response = new ComentariosResponse();
        response.setComentarios(comentarios);
        response.setTotalComentarios(comentarios.size());
        return response;
    }

    private String nombreAutor(Comentario comentario) {
        if (comentario.getParte() != null) {
            return comentario.getParte().getNombreParte();
        }
        Usuario usuario = comentario.getUsuario();
        return usuario == null ? null : usuario.getNombre() + " " + usuario.getApellido();
    }

    private ComentarioResponse toResponse(Comentario comentario) {
        ComentarioResponse response = new ComentarioResponse();
        response.setIdComentario(comentario.getIdComentario());
        response.setAutor(nombreAutor(comentario));
        response.setFechaPublicacion(comentario.getFechaPublicacion());
        response.setTexto(comentario.getTexto());
        response.setTextoSeleccionado(comentario.getTextoSeleccionado());
        return response;
    }
}
