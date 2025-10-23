// language: java
package com.registo.horas_estagio.service.impl;

import com.registo.horas_estagio.models.JwtToken;
import com.registo.horas_estagio.repository.JwtTokenRepository;
import com.registo.horas_estagio.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtTokenRepository repository;

    @Override
    public void saveToken(String token, String username, Instant issuedAt, Instant expiresAt) {
        JwtToken jwt = new JwtToken();
        jwt.setToken(token);
        jwt.setUsername(username);
        jwt.setIssuedAt(issuedAt);
        jwt.setExpiresAt(expiresAt);
        jwt.setRevoked(false);
        repository.save(jwt);
    }

    @Override
    public Optional<JwtToken> findByToken(String token) {
        return repository.findByToken(token);
    }

    @Override
    public void revokeToken(String token) {
        repository.findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            repository.save(t);
        });
    }

    @Override
    public boolean isTokenActive(String token) {
        return repository.findByToken(token)
                .filter(t -> !t.isRevoked())
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
    }
    // Novo: busca o token mais recente do usuário
    @Override
    public Optional<JwtToken> findLatestTokenByUsername(String username) {
        return repository.findTopByUsernameOrderByIssuedAtDesc(username);
    }

    @Override
    public Optional<JwtToken> getReusableTokenForUser(String username) {
        return findLatestTokenByUsername(username)
                                .filter(t -> !t.isRevoked())
                               .filter(t -> !isExpired(t));
    }

}