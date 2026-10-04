package com.firmaya.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

import com.firmaya.core.dto.FirmaRegistradaResponse;
import com.firmaya.core.entity.Contrato;
import com.firmaya.core.entity.EstadoContrato;
import com.firmaya.core.entity.Firma;
import com.firmaya.core.entity.PreferenciaNotificacion;
import com.firmaya.core.entity.TokenSeguridad;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.entity.UsuarioContrato;
import com.firmaya.core.entity.VersionContrato;
import com.firmaya.core.exception.NoAutenticadoException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.ContratoRepository;
import com.firmaya.core.repository.EstadoContratoRepository;
import com.firmaya.core.repository.FirmaRepository;
import com.firmaya.core.repository.TokenSeguridadRepository;
import com.firmaya.core.repository.UsuarioContratoRepository;

class FirmaOtpServiceTest {

    private static final String TOKEN = "token-firma";

    private FirmaRepository firmaRepository;
    private TokenSeguridadRepository tokenSeguridadRepository;
    private UsuarioContratoRepository usuarioContratoRepository;
    private EstadoContratoRepository estadoContratoRepository;
    private NotificacionService notificacionService;
    private FirmaOtpService service;
    private Firma firma;
    private UsuarioContrato parte;
    private Contrato contrato;
    private Usuario creador;

    @BeforeEach
    void preparar() {
        firmaRepository = mock(FirmaRepository.class);
        tokenSeguridadRepository = mock(TokenSeguridadRepository.class);
        usuarioContratoRepository = mock(UsuarioContratoRepository.class);
        estadoContratoRepository = mock(EstadoContratoRepository.class);
        notificacionService = mock(NotificacionService.class);
        service = new FirmaOtpService(firmaRepository, tokenSeguridadRepository, usuarioContratoRepository,
                mock(ContratoRepository.class), estadoContratoRepository, notificacionService,
                mock(AuditoriaService.class), new GeneradorToken());

        creador = new Usuario();
        creador.setEmail("maria@mail.com");
        EstadoContrato listo = new EstadoContrato();
        listo.setNombre(EstadoContrato.LISTO_PARA_FIRMAR);
        contrato = new Contrato();
        contrato.setIdContrato(10);
        contrato.setNombre("Locación");
        contrato.setEstado(listo);
        contrato.setUsuarioCreador(creador);

        parte = new UsuarioContrato();
        parte.setIdParte(1);
        parte.setContrato(contrato);
        parte.setNombreParte("Ana");
        parte.setCorreoInvitado("ana@mail.com");
        parte.setRolParte(UsuarioContrato.ROL_FIRMANTE);

        VersionContrato version = new VersionContrato();
        version.setNumeroVersion(3);
        version.setHashSha256("hash-v3");

        firma = new Firma();
        firma.setParte(parte);
        firma.setVersion(version);
        firma.setEstadoFirma(Firma.ESTADO_NOTIFICADO);
        firma.setTokenFirma(TOKEN);
        firma.setFechaExpiracionToken(LocalDateTime.now().plusMinutes(10));
        firma.setIntentosOtpFallidos(0);
        when(firmaRepository.findByTokenFirma(TOKEN)).thenReturn(Optional.of(firma));
    }

    @Test
    void enlaceVencidoOBloqueadoEsRechazado() {
        firma.setFechaExpiracionToken(LocalDateTime.now().minusMinutes(1));
        NoAutenticadoException vencido = assertThrows(NoAutenticadoException.class,
                () -> service.obtenerContratoAFirmar(TOKEN));
        assertEquals(FirmaOtpService.MENSAJE_ENLACE_INVALIDO, vencido.getMessage());

        firma.setFechaExpiracionToken(LocalDateTime.now().plusMinutes(10));
        firma.setEnlaceBloqueado(true);
        ReglaNegocioException bloqueado = assertThrows(ReglaNegocioException.class,
                () -> service.obtenerContratoAFirmar(TOKEN));
        assertEquals(FirmaOtpService.MENSAJE_ENLACE_BLOQUEADO, bloqueado.getMessage());
    }

    @Test
    void enviarCodigoGeneraOtpDe6DigitosQueVenceEn10Minutos() {
        TokenSeguridad anterior = new TokenSeguridad();
        anterior.setUsado(false);
        when(tokenSeguridadRepository.findByIdParteAndTipoTokenAndUsadoFalse(1, TokenSeguridad.TIPO_OTP))
                .thenReturn(Arrays.asList(anterior));

        String mensaje = service.enviarCodigo(TOKEN).getMensaje();

        ArgumentCaptor<TokenSeguridad> captor = ArgumentCaptor.forClass(TokenSeguridad.class);
        verify(tokenSeguridadRepository).save(captor.capture());
        TokenSeguridad otp = captor.getValue();
        assertTrue(otp.getValor().matches("\\d{6}"));
        assertEquals(Integer.valueOf(1), otp.getIdParte());
        assertEquals(otp.getFechaCreacion().plusMinutes(10), otp.getFechaExpiracion());
        assertTrue(anterior.getUsado());
        assertEquals("Ingrese el código de 6 dígitos enviado a ana@mail.com. El código expira en 10 minutos",
                mensaje);
    }

    @Test
    void codigoIncorrectoDescuentaIntentosYBloqueaAlTercero() {
        prepararOtp("123456", LocalDateTime.now().plusMinutes(5));

        ReglaNegocioException primero = assertThrows(ReglaNegocioException.class,
                () -> service.confirmarFirma(TOKEN, "000000", "1.1.1.1"));
        assertTrue(primero.getMessage().endsWith("Intentos restantes: 2."));

        assertThrows(ReglaNegocioException.class, () -> service.confirmarFirma(TOKEN, "000000", "1.1.1.1"));
        ReglaNegocioException tercero = assertThrows(ReglaNegocioException.class,
                () -> service.confirmarFirma(TOKEN, "000000", "1.1.1.1"));
        assertEquals(FirmaOtpService.MENSAJE_ENLACE_BLOQUEADO, tercero.getMessage());
        assertTrue(firma.getEnlaceBloqueado());
    }

    @Test
    void codigoExpiradoPideUnoNuevo() {
        prepararOtp("123456", LocalDateTime.now().minusMinutes(1));

        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> service.confirmarFirma(TOKEN, "123456", "1.1.1.1"));

        assertEquals(FirmaOtpService.MENSAJE_CODIGO_EXPIRADO, ex.getMessage());
    }

    @Test
    void codigoCorrectoRegistraFirmaYSiFirmaronTodosElContratoQuedaFirmado() {
        TokenSeguridad otp = prepararOtp("123456", LocalDateTime.now().plusMinutes(5));
        when(usuarioContratoRepository.findByContratoIdContratoAndRolParteOrderByIdParte(10,
                UsuarioContrato.ROL_FIRMANTE)).thenReturn(Arrays.asList(parte));
        when(firmaRepository.findFirstByParteIdParteOrderByIdFirmaDesc(1)).thenReturn(Optional.of(firma));
        EstadoContrato firmado = new EstadoContrato();
        firmado.setNombre(EstadoContrato.FIRMADO);
        when(estadoContratoRepository.findByNombre(EstadoContrato.FIRMADO)).thenReturn(Optional.of(firmado));

        FirmaRegistradaResponse response = service.confirmarFirma(TOKEN, "123456", "181.44.10.22");

        assertEquals(Firma.ESTADO_FIRMADO, firma.getEstadoFirma());
        assertEquals("hash-v3", firma.getHashFirma());
        assertEquals("181.44.10.22", firma.getDireccionIp());
        assertTrue(otp.getUsado());
        assertTrue(response.isContratoFirmado());
        assertEquals(EstadoContrato.FIRMADO, contrato.getEstado().getNombre());
        assertEquals(FirmaOtpService.MENSAJE_FIRMA_REGISTRADA, response.getMensaje());
        verify(notificacionService).notificarEvento(eq(creador), eq("maria@mail.com"), eq(10),
                eq(PreferenciaNotificacion.EVENTO_FIRMA_RECIBIDA), anyString(), anyString());
    }

    @Test
    void siFaltanFirmantesElContratoSigueListoParaFirmar() {
        prepararOtp("123456", LocalDateTime.now().plusMinutes(5));
        UsuarioContrato otro = new UsuarioContrato();
        otro.setIdParte(2);
        when(usuarioContratoRepository.findByContratoIdContratoAndRolParteOrderByIdParte(10,
                UsuarioContrato.ROL_FIRMANTE)).thenReturn(Arrays.asList(parte, otro));
        when(firmaRepository.findFirstByParteIdParteOrderByIdFirmaDesc(1)).thenReturn(Optional.of(firma));
        when(firmaRepository.findFirstByParteIdParteOrderByIdFirmaDesc(2)).thenReturn(Optional.<Firma>empty());

        FirmaRegistradaResponse response = service.confirmarFirma(TOKEN, "123456", "1.1.1.1");

        assertFalse(response.isContratoFirmado());
        assertEquals(EstadoContrato.LISTO_PARA_FIRMAR, contrato.getEstado().getNombre());
    }

    private TokenSeguridad prepararOtp(String valor, LocalDateTime expiracion) {
        TokenSeguridad otp = new TokenSeguridad();
        otp.setValor(valor);
        otp.setUsado(false);
        otp.setFechaExpiracion(expiracion);
        when(tokenSeguridadRepository.findFirstByIdParteAndTipoTokenAndUsadoFalseOrderByIdTokenDesc(1,
                TokenSeguridad.TIPO_OTP)).thenReturn(Optional.of(otp));
        when(tokenSeguridadRepository.findByIdParteAndTipoTokenAndUsadoFalse(any(), anyString()))
                .thenReturn(Collections.<TokenSeguridad>emptyList());
        return otp;
    }
}
