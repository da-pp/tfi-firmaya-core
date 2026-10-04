package com.firmaya.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firmaya.core.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdUsuarioNot(String email, Integer idUsuario);
}
