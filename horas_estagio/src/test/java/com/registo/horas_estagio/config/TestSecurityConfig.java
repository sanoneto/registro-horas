package com.registo.horas_estagio.config;

import jakarta.servlet.*;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.io.IOException;

/**
 * Configuração usada apenas nos testes para fornecer beans necessários ao contexto
 * e evitar falhas de carregamento relacionadas à segurança real (JWT, filtros, etc).
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Um UserDetailsService simples em memória para permitir carregamento do contexto
     * sem depender do banco ou do UserDetailsService real.
     */
    @Bean
    @Primary
    public UserDetailsService testUserDetailsService(PasswordEncoder encoder) {
        // cria um usuário padrão "test" com role ROLE_ADMIN (senha: test123)
        var user = User.withUsername("test")
                .password(encoder.encode("test123"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Filtro stub para substituir o JwtAuthenticationFilter real durante testes.
     * Ele simplesmente delega a chain sem tentar validar tokens.
     */
    @Bean
    @Primary
    public Filter jwtAuthenticationFilterStub() {
        return new Filter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
                // no-op
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                // não altera a autenticação, apenas segue o fluxo
                chain.doFilter(request, response);
            }

            @Override
            public void destroy() {
                // no-op
            }
        };
    }
}