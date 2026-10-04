package com.firmaya.core.entity;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Preferencias de notificación de un usuario (CU-20).
 * - Fila de evento: tipo_evento informado, canal nulo; activa = interruptor del evento.
 * - Fila de canal: canal informado, tipo_evento nulo; activa = canal seleccionado.
 */
@Entity
@Table(name = "preferencia_notificacion")
public class PreferenciaNotificacion {

    // Eventos configurables (CU-20)
    public static final String EVENTO_NUEVA_VERSION = "Nueva versión publicada";
    public static final String EVENTO_FIRMA_RECIBIDA = "Firma recibida";
    public static final String EVENTO_LISTO_PARA_FIRMAR = "Contrato listo para firmar";
    public static final String EVENTO_NUEVO_COMENTARIO = "Nuevo comentario";
    public static final String EVENTO_CAMBIO_ESTADO = "Cambio de estado";

    // Canales (CU-20 paso 6)
    public static final String CANAL_CORREO = "Correo electrónico";
    public static final String CANAL_PLATAFORMA = "Notificación de Plataforma";

    public static final List<String> EVENTOS = Arrays.asList(EVENTO_NUEVA_VERSION, EVENTO_FIRMA_RECIBIDA,
            EVENTO_LISTO_PARA_FIRMAR, EVENTO_NUEVO_COMENTARIO, EVENTO_CAMBIO_ESTADO);
    public static final List<String> CANALES = Arrays.asList(CANAL_CORREO, CANAL_PLATAFORMA);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_preferencia")
    private Integer idPreferencia;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "canal", length = 50)
    private String canal;

    @Column(name = "activa")
    private Boolean activa;

    // CU-20 (ampliación I10)
    @Column(name = "tipo_evento", length = 50)
    private String tipoEvento;

    public Integer getIdPreferencia() {
        return idPreferencia;
    }

    public void setIdPreferencia(Integer idPreferencia) {
        this.idPreferencia = idPreferencia;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }
}
