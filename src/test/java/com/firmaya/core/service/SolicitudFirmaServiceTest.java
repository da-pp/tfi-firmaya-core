package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.firmaya.core.dto.EstadoFirmasResponse;
import com.firmaya.core.dto.SolicitarFirmasRequest;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.Firma;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.UsuarioContrato;
import com.firmaya.core.entity.VersionContrato;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.FirmaRepository;
import com.firmaya.core.repository.UsuarioContratoRepository;

class SolicitudFirmaServiceTest {

    private FirmaRepository firmaRepository;
    private UsuarioContratoRepository usuarioContratoRepository;
    private ContratoService contratoService;
    private NotificacionService notificacionService;
    private SolicitudFirmaService service;
    private Usuario usuario = new Usuario();
    private Contrato contrato;
    private UsuarioContrato ana;
    private UsuarioContrato juan;
    // Firmas "guardadas" por id de parte
    private final Map<Integer, Firma> firmas = new HashMap<>();

    @BeforeEach
    void preparar() {
        firmaRepository = mock(FirmaRepository.class);
        usuarioContratoRepository = mock(UsuarioContratoRepository.class);
        contratoService = mock(ContratoService.class);
        notificacionService = mock(NotificacionService.class);
        service = new SolicitudFirmaService(firmaRepository, usuarioContratoRepository, contratoService,
                notificacionService, mock(AuditoriaService.class), new GeneradorToken(), "http://localhost:3000");

        EstadoContrato estado = new EstadoContrato();
        estado.setNombre(EstadoContrato.LISTO_PARA_FIRMAR);
        contrato = new Contrato();
        contrato.setIdContrato(10);
        contrato.setNombre("Locación");
        contrato.setEstado(estado);
        when(contratoService.buscarContrato(10)).thenReturn(contrato);
        when(contratoService.versionActual(10)).thenReturn(new VersionContrato());

        ana = parte(1, "ana@mail.com");
        juan = parte(2, "juan@mail.com");
        when(usuarioContratoRepository.findByContratoIdContratoAndRolParteOrderByIdParte(10,
                UsuarioContrato.ROL_FIRMANTE)).thenReturn(Arrays.asList(ana, juan));

        when(firmaRepository.save(any(Firma.class))).thenAnswer(invocacion -> {
            Firma firma = invocacion.getArgument(0);
            if (firma.getIdFirma() == null) {
                firma.setIdFirma(100 + firma.getParte().getIdParte());
            }
            firmas.put(firma.getParte().getIdParte(), firma);
            return firma;
        });
        when(firmaRepository.findFirstByParteIdParteOrderByIdFirmaDesc(anyInt()))
                .thenAnswer(invocacion -> Optional.ofNullable(firmas.get((Integer) invocacion.getArgument(0))));
    }

    @Test
    void solicitarFirmasNotificaACadaFirmante() {
        when(notificacionService.enviarCorreo(any(), any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);

        EstadoFirmasResponse response = service.solicitarFirmas(10, request(null), usuario, "10.0.0.1");

        assertEquals(2, response.getFirmantes().size());
        assertEquals(Firma.ESTADO_NOTIFICADO, response.getFirmantes().get(0).getEstadoFirma());
        assertTrue(response.getFirmantes().get(0).getEnlace().startsWith("http://localhost:3000/firmar/"));
        assertEquals("0 de 2 firmas completadas", response.getProgreso());
        assertEquals(SolicitudFirmaService.MENSAJE_SOLICITUDES_ENVIADAS, response.getMensaje());
    }

    @Test
    void falloDeEnvioDejaPendienteYSinMensajeDeExito() {
        when(notificacionService.enviarCorreo(any(), any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);

        EstadoFirmasResponse response = service.solicitarFirmas(10, request(null), usuario, "10.0.0.1");

        assertEquals(Firma.ESTADO_PENDIENTE, response.getFirmantes().get(0).getEstadoFirma());
        assertEquals(Boolean.FALSE, response.getFirmantes().get(0).getEnvioExitoso());
        assertEquals(null, response.getMensaje());
    }

    @Test
    void validacionesDeLaSolicitud() {
        String hoy = LocalDate.now().format(Fechas.DD_MM_AAAA);
        ReglaNegocioException fecha = assertThrows(ReglaNegocioException.class,
                () -> service.solicitarFirmas(10, request(hoy), usuario, "10.0.0.1"));
        assertEquals(SolicitarFirmasRequest.MENSAJE_FECHA_LIMITE, fecha.getMessage());

        when(usuarioContratoRepository.findByContratoIdContratoAndRolParteOrderByIdParte(10,
                UsuarioContrato.ROL_FIRMANTE)).thenReturn(Collections.<UsuarioContrato>emptyList());
        ReglaNegocioException sinFirmantes = assertThrows(ReglaNegocioException.class,
                () -> service.solicitarFirmas(10, request(null), usuario, "10.0.0.1"));
        assertEquals(SolicitudFirmaService.MENSAJE_SIN_FIRMANTES, sinFirmantes.getMessage());

        contrato.getEstado().setNombre(EstadoContrato.EN_REVISION);
        assertThrows(ReglaNegocioException.class,
                () -> service.solicitarFirmas(10, request(null), usuario, "10.0.0.1"));
    }

    @Test
    void estadoSinSolicitudesYConTodasCompletadas() {
        when(firmaRepository.findByParteContratoIdContrato(10)).thenReturn(Collections.<Firma>emptyList());
        assertEquals(SolicitudFirmaService.MENSAJE_SIN_SOLICITUDES, service.consultarEstado(10).getMensaje());

        firmas.put(1, firmada(ana));
        firmas.put(2, firmada(juan));
        when(firmaRepository.findByParteContratoIdContrato(10)).thenReturn(Arrays.asList(firmas.get(1)));
        EstadoFirmasResponse response = service.consultarEstado(10);
        assertEquals("2 de 2 firmas completadas", response.getProgreso());
        assertEquals(SolicitudFirmaService.MENSAJE_TODAS_COMPLETADAS, response.getMensaje());
        assertEquals("181.44.10.22", response.getFirmantes().get(0).getDireccionIp());
    }

    @Test
    void reenviarMarcaReNotificadoOInformaElError() {
        Firma firma = new Firma();
        firma.setIdFirma(101);
        firma.setParte(ana);
        firma.setEstadoFirma(Firma.ESTADO_NOTIFICADO);
        firmas.put(1, firma);
        when(firmaRepository.findByIdFirmaAndParteContratoIdContrato(101, 10)).thenReturn(Optional.of(firma));

        when(notificacionService.enviarCorreo(any(), any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(true);
        EstadoFirmasResponse ok = service.reenviarSolicitud(10, 101, usuario, "10.0.0.1");
        assertEquals(Firma.ESTADO_RENOTIFICADO, firma.getEstadoFirma());
        assertEquals(SolicitudFirmaService.MENSAJE_REENVIADA, ok.getMensaje());

        when(notificacionService.enviarCorreo(any(), any(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(false);
        EstadoFirmasResponse error = service.reenviarSolicitud(10, 101, usuario, "10.0.0.1");
        assertEquals(SolicitudFirmaService.MENSAJE_ERROR_REENVIO, error.getMensaje());
    }

    private Firma firmada(UsuarioContrato parte) {
        Firma firma = new Firma();
        firma.setIdFirma(100 + parte.getIdParte());
        firma.setParte(parte);
        firma.setEstadoFirma(Firma.ESTADO_FIRMADO);
        firma.setDireccionIp("181.44.10.22");
        return firma;
    }

    private UsuarioContrato parte(int id, String correo) {
        UsuarioContrato parte = new UsuarioContrato();
        parte.setIdParte(id);
        parte.setContrato(contrato);
        parte.setCorreoInvitado(correo);
        parte.setNombreParte("Parte " + id);
        parte.setRolParte(UsuarioContrato.ROL_FIRMANTE);
        return parte;
    }

    private SolicitarFirmasRequest request(String fechaLimite) {
        SolicitarFirmasRequest request = new SolicitarFirmasRequest();
        request.setCanal("Correo electrónico");
        request.setFechaLimite(fechaLimite);
        return request;
    }
}
