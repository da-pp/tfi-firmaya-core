package com.firmaya.core.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.firmaya.core.entity.Auditoria;
import com.firmaya.core.entity.Usuario;
import com.firmaya.core.repository.AuditoriaRepository;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public void registrar(Usuario usuario, String tipoAccion, String entidadAfectada,
            Integer idEntidadAfectada, String descripcion, String direccionIp) {
        guardar(usuario, tipoAccion, entidadAfectada, idEntidadAfectada, descripcion, direccionIp, null, null, null);
    }

    public void registrarCambio(Usuario usuario, String tipoAccion, String entidadAfectada,
            Integer idEntidadAfectada, String descripcion, String direccionIp, String datosAntes,
            String datosDespues) {
        guardar(usuario, tipoAccion, entidadAfectada, idEntidadAfectada, descripcion, direccionIp, datosAntes,
                datosDespues, null);
    }

    public void registrarEnContrato(Usuario usuario, String tipoAccion, Integer idContrato, Integer idVersion,
            String descripcion, String direccionIp, String datosAntes, String datosDespues) {
        guardar(usuario, tipoAccion, AuditoriaConsultaService.ENTIDAD_CONTRATO, idContrato, descripcion,
                direccionIp, datosAntes, datosDespues, idVersion);
    }

    private void guardar(Usuario usuario, String tipoAccion, String entidadAfectada, Integer idEntidadAfectada,
            String descripcion, String direccionIp, String datosAntes, String datosDespues, Integer idVersion) {
        Auditoria auditoria = new Auditoria();
        auditoria.setUsuario(usuario);
        auditoria.setTipoAccion(tipoAccion);
        auditoria.setEntidadAfectada(entidadAfectada);
        auditoria.setIdEntidadAfectada(idEntidadAfectada);
        auditoria.setDescripcion(descripcion);
        auditoria.setDireccionIp(direccionIp);
        auditoria.setDatosAntes(datosAntes);
        auditoria.setDatosDespues(datosDespues);
        auditoria.setIdVersion(idVersion);
        auditoria.setFechaHora(LocalDateTime.now());
        auditoriaRepository.save(auditoria);
    }
}
