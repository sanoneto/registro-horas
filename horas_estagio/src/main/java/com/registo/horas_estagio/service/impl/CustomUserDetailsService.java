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
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario user = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        log.info("Carregando usuário: {}", user.getUsername());

        Collection<? extends GrantedAuthority> authorities = buildAuthorities(user);

        log.info("Role do banco: {}", user.getRole());
        log.info("Authorities configuradas: {}", authorities);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }

    /**
     * Constrói a coleção de GrantedAuthority para o usuário.
     * Normaliza a role (adiciona prefixo ROLE_ se necessário) e valida sua presença.
     *
     * @param user usuário carregado do repositório
     * @return lista imutável com as authorities
     * @throws UsernameNotFoundException quando role estiver ausente/inválida
     */
    private Collection<? extends GrantedAuthority> buildAuthorities(Usuario user) {
        String role = user.getRole();

        if (role == null || role.isBlank()) {
            log.error("Role ausente para usuário: {}", user.getUsername());
            // Lança UsernameNotFoundException para manter consistência com UserDetailsService
            throw new UsernameNotFoundException("Usuário sem role definida: " + user.getUsername());
        }

        String normalizedRole = role.toUpperCase(Locale.ROOT);
        if (!normalizedRole.startsWith("ROLE_")) {
            normalizedRole = "ROLE_" + normalizedRole;
        }

        log.debug("Authority final gerada para usuário {}: {}", user.getUsername(), normalizedRole);
        return List.of(new SimpleGrantedAuthority(normalizedRole));
    }
}