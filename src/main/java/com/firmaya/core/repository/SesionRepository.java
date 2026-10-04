package com.firmaya.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firmaya.core.entity.Sesion;

public interface SesionRepository extends JpaRepository<Sesion, Integer> {

    Optional<Sesion> findByTokenAndEstado(String token, String estado);

    List<Sesion> findByUsuarioIdUsuarioAndEstado(Integer idUsuario, String estado);
}
