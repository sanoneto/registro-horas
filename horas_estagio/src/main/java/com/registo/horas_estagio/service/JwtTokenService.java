package com.registo.horas_estagio.service;

import com.registo.horas_estagio.models.JwtToken;

import java.time.Instant;
import java.util.Optional;

public interface JwtTokenService {
    void saveToken(String token, String username, Instant issuedAt, Instant expiresAt);

    Optional<JwtToken> findByToken(String token);

    void revokeToken(String token);

    boolean isTokenActive(String token);

    // (opcionalmente) utilitário para checar expirado
    default boolean isExpired(JwtToken jwtToken) {
        return jwtToken == null || jwtToken.getExpiresAt().isBefore(Instant.now());
    }

    Optional<JwtToken> getReusableTokenForUser(String username);
}