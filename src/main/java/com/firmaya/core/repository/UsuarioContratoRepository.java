package com.firmaya.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firmaya.core.entity.UsuarioContrato;

public interface UsuarioContratoRepository extends JpaRepository<UsuarioContrato, Integer> {

    List<UsuarioContrato> findByContratoIdContratoOrderByIdParte(Integer idContrato);

    boolean existsByContratoIdContratoAndCorreoInvitadoIgnoreCase(Integer idContrato, String correo);

    Optional<UsuarioContrato> findByIdParteAndContratoIdContrato(Integer idParte, Integer idContrato);

    Optional<UsuarioContrato> findByTokenInvitacion(String tokenInvitacion);

    boolean existsByContratoIdContratoAndRolParte(Integer idContrato, String rolParte);

    List<UsuarioContrato> findByContratoIdContratoAndRolParteOrderByIdParte(Integer idContrato, String rolParte);
}
