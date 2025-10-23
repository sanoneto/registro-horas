// language: java
package com.registo.horas_estagio.service.impl;

import com.registo.horas_estagio.models.JwtToken;
import com.registo.horas_estagio.models.Usuario;
import com.registo.horas_estagio.repository.JwtTokenRepository;
import com.registo.horas_estagio.repository.UsuarioRepository;
import com.registo.horas_estagio.service.JwtTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {

    private final JwtTokenRepository jwtTokenRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public void saveToken(String token, String username, Instant issuedAt, Instant expiresAt) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario não encontrado: " + username));
        JwtToken jwtToken = new JwtToken();
        jwtToken.setToken(token);
        jwtToken.setIssuedAt(issuedAt);
        jwtToken.setExpiresAt(expiresAt);
        jwtToken.setRevoked(false);
        jwtToken.setUsuario(usuario);
        jwtTokenRepository.save(jwtToken);
    }

    @Override
    public Optional<JwtToken> findByToken(String token) {
        return jwtTokenRepository.findByToken(token);
    }

    @Override
    public void revokeToken(String token) {
        jwtTokenRepository.findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            jwtTokenRepository.save(t);
        });
    }

    @Override
    public boolean isTokenActive(String token) {
        return jwtTokenRepository.findByToken(token)
                .filter(t -> !t.isRevoked())
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
    }
    @Override
    public Optional<JwtToken> getReusableTokenForUser(String username) {
        Instant now = Instant.now();
        return jwtTokenRepository
                .findTopByUsuario_UsernameAndRevokedFalseOrderByExpiresAtDesc(username)
                .filter(t -> t.getExpiresAt().isAfter(now));
    }

}