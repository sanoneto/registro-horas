package com.registo.horas_estagio.repository;

import com.registo.horas_estagio.models.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {
    Optional<JwtToken> findByToken(String token);
    // Novo: busca o token mais recente para um username (se houver)
    Optional<JwtToken> findTopByUsernameOrderByIssuedAtDesc(String username);
}