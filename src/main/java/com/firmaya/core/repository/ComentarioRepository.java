package com.firmaya.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firmaya.core.entity.Comentario;

public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {

    List<Comentario> findByContratoIdContratoOrderByFechaPublicacionAscIdComentarioAsc(Integer idContrato);

    long countByContratoIdContrato(Integer idContrato);
}
