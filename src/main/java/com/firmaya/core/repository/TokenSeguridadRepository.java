package com.firmaya.core.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.firmaya.core.entity.TokenSeguridad;

public interface TokenSeguridadRepository extends JpaRepository<TokenSeguridad, Integer> {

    Optional<TokenSeguridad> findByValorAndTipoTokenIn(String valor, Collection<String> tiposToken);

    // Códigos OTP sin usar de una parte (CU-08)
    List<TokenSeguridad> findByIdParteAndTipoTokenAndUsadoFalse(Integer idParte, String tipoToken);

    Optional<TokenSeguridad> findFirstByIdParteAndTipoTokenAndUsadoFalseOrderByIdTokenDesc(Integer idParte,
            String tipoToken);
}
