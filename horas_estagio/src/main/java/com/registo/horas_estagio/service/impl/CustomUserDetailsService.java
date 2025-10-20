package com.registo.horas_estagio.service.impl;

import com.registo.horas_estagio.models.Usuario;
import com.registo.horas_estagio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UsuarioRepository usuarioRepository; // Seu repositório de usuários

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Busque o usuário no banco de dados
        Usuario user = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);

        // Log para debug (IMPORTANTE PARA DIAGNOSTICAR)
        log.info(" Carregando usuário: {}", user.getUsername());
        log.info(" Role do banco: {}", user.getRole());
        log.info(" Authorities configuradas: {}", authorities);

        // Retorne um UserDetails do Spring Security
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword()) // Deve estar criptografada com BCrypt
                .authorities(authorities) // ou .roles("USER", "ADMIN")
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Usuario user) {
        String role = user.getRole();

        // Validação para role null (não deveria ocorrer devido à constraint no banco)
        if (role == null || role.isBlank()) {
            log.error("Role null ou vazia para usuário: {}", user.getUsername());
            throw new IllegalStateException("Usuário sem role definida: " + user.getUsername());
        }

        // Se a role no banco já tem o prefixo ROLE_, usa diretamente
        // Caso contrário, adiciona o prefixo
        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }

        log.info(" Authority final: {}", role);
        return List.of(new SimpleGrantedAuthority(role));
    }
}