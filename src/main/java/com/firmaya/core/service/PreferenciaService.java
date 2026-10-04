package com.firmaya.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firmaya.core.dto.OpcionPreferencia;
import com.firmaya.core.dto.PreferenciasRequest;
import com.firmaya.core.dto.PreferenciasResponse;
import com.firmaya.core.entity.PreferenciaNotificacion;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.exception.ConfirmacionRequeridaException;
import com.firmaya.core.exception.ReglaNegocioException;
import com.firmaya.core.repository.PreferenciaNotificacionRepository;

/**
 * CU-20 – Configurar notificaciones.
 * Un evento o canal sin preferencia guardada se considera activo.
 */
@Service
public class PreferenciaService {

    static final String MENSAJE_SIN_CANAL = "Debe seleccionar al menos un canal de notificación.";
    static final String MENSAJE_TODO_DESACTIVADO = "Tiene todas las notificaciones desactivadas. No recibirá alertas "
            + "de actividad en sus contratos. ¿Confirma esta configuración?";
    static final String MENSAJE_GUARDADAS = "Preferencias de notificación guardadas exitosamente.";

    private final PreferenciaNotificacionRepository preferenciaRepository;

    public PreferenciaService(PreferenciaNotificacionRepository preferenciaRepository) {
        this.preferenciaRepository = preferenciaRepository;
    }

    // Pasos 4 a 7
    @Transactional(readOnly = true)
    public PreferenciasResponse obtenerPreferencias(Usuario usuario) {
        PreferenciasResponse response = new PreferenciasResponse();
        response.setEventos(opciones(usuario, PreferenciaNotificacion.EVENTOS, true));
        response.setCanales(opciones(usuario, PreferenciaNotificacion.CANALES, false));
        return response;
    }

    /**
     * Pasos 11 a 16 y caminos alternativos.
     */
    @Transactional
    public PreferenciasResponse guardarPreferencias(Usuario usuario, PreferenciasRequest request) {
        // Valores actuales, reemplazados por los que llegan en la solicitud
        Map<String, Boolean> eventos = valores(obtenerPreferencias(usuario).getEventos());
        Map<String, Boolean> canales = valores(obtenerPreferencias(usuario).getCanales());
        aplicar(request.getEventos(), eventos, "eventos", "El evento no es válido: ");
        aplicar(request.getCanales(), canales, "canales", "El canal no es válido: ");

        // Paso 12: al menos un canal seleccionado
        if (!canales.containsValue(true)) {
            throw new ReglaNegocioException("canales", MENSAJE_SIN_CANAL);
        }
        // Camino alternativo: todos los eventos desactivados requiere confirmación
        if (!eventos.containsValue(true) && !request.isConfirmarTodoDesactivado()) {
            throw new ConfirmacionRequeridaException(MENSAJE_TODO_DESACTIVADO);
        }

        // Paso 13
        preferenciaRepository.deleteByUsuarioIdUsuario(usuario.getIdUsuario());
        List<PreferenciaNotificacion> filas = new ArrayList<>();
        for (String evento : PreferenciaNotificacion.EVENTOS) {
            filas.add(fila(usuario, evento, null, eventos.get(evento)));
        }
        for (String canal : PreferenciaNotificacion.CANALES) {
            filas.add(fila(usuario, null, canal, canales.get(canal)));
        }
        preferenciaRepository.saveAll(filas);

        // Pasos 14 y 16
        PreferenciasResponse response = new PreferenciasResponse();
        response.setEventos(aOpciones(PreferenciaNotificacion.EVENTOS, eventos));
        response.setCanales(aOpciones(PreferenciaNotificacion.CANALES, canales));
        response.setMensaje(MENSAJE_GUARDADAS);
        return response;
    }

    public boolean eventoActivo(Usuario usuario, String evento) {
        return preferenciaRepository
                .findFirstByUsuarioIdUsuarioAndTipoEventoAndCanalIsNull(usuario.getIdUsuario(), evento)
                .map(p -> Boolean.TRUE.equals(p.getActiva()))
                .orElse(true);
    }

    public boolean canalActivo(Usuario usuario, String canal) {
        return preferenciaRepository
                .findFirstByUsuarioIdUsuarioAndCanalAndTipoEventoIsNull(usuario.getIdUsuario(), canal)
                .map(p -> Boolean.TRUE.equals(p.getActiva()))
                .orElse(true);
    }

    private List<OpcionPreferencia> opciones(Usuario usuario, List<String> nombres, boolean sonEventos) {
        List<OpcionPreferencia> lista = new ArrayList<>();
        for (String nombre : nombres) {
            boolean activo = sonEventos ? eventoActivo(usuario, nombre) : canalActivo(usuario, nombre);
            lista.add(new OpcionPreferencia(nombre, activo));
        }
        return lista;
    }

    private void aplicar(List<OpcionPreferencia> solicitadas, Map<String, Boolean> valores, String campo,
            String mensajeInvalido) {
        if (solicitadas == null) {
            return;
        }
        for (OpcionPreferencia opcion : solicitadas) {
            if (!valores.containsKey(opcion.getNombre())) {
                throw new ReglaNegocioException(campo, mensajeInvalido + opcion.getNombre());
            }
            valores.put(opcion.getNombre(), opcion.isActivo());
        }
    }

    private Map<String, Boolean> valores(List<OpcionPreferencia> opciones) {
        Map<String, Boolean> valores = new HashMap<>();
        for (OpcionPreferencia opcion : opciones) {
            valores.put(opcion.getNombre(), opcion.isActivo());
        }
        return valores;
    }

    private List<OpcionPreferencia> aOpciones(List<String> nombres, Map<String, Boolean> valores) {
        List<OpcionPreferencia> lista = new ArrayList<>();
        for (String nombre : nombres) {
            lista.add(new OpcionPreferencia(nombre, valores.get(nombre)));
        }
        return lista;
    }

    private PreferenciaNotificacion fila(Usuario usuario, String evento, String canal, boolean activa) {
        PreferenciaNotificacion fila = new PreferenciaNotificacion();
        fila.setUsuario(usuario);
        fila.setTipoEvento(evento);
        fila.setCanal(canal);
        fila.setActiva(activa);
        return fila;
    }
}
