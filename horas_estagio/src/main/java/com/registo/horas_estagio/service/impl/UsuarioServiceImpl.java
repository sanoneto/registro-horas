package com.registo.horas_estagio.service.impl;

import com.registo.horas_estagio.models.Usuario;
import com.registo.horas_estagio.repository.UsuarioRepository;
import com.registo.horas_estagio.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository; // Repositório dos usuários.
    private final PasswordEncoder passwordEncoder;     // Para a codificação da senha.

    @Override
    public Usuario registrarUsuario(Usuario usuario) {
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Override
    public boolean existeUsuario(String username) {
        return usuarioRepository.findByUsername(username).isPresent();
    }

}
