// language: java
package com.registo.horas_estagio.security;

import com.registo.horas_estagio.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SecurityConfig - Unit tests (sem Spring Context)")
class SecurityConfigUnitTest {

    @Test
    @DisplayName("passwordEncoder() deve retornar BCryptPasswordEncoder funcional")
    void shouldReturnBCryptPasswordEncoder() {
        // Arrange
        UserDetailsService uds = Mockito.mock(UserDetailsService.class);
        SecurityConfig config = new SecurityConfig(uds);

        // Act
        PasswordEncoder encoder = config.passwordEncoder();

        // Assert
        assertThat(encoder).isNotNull();
        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);

        String raw = "senha123";
        String encoded = encoder.encode(raw);

        assertThat(encoded).isNotBlank();
        assertThat(encoder.matches(raw, encoded)).isTrue();
        assertThat(encoder.matches("outraSenha", encoded)).isFalse();
    }

    @Test
    @DisplayName("authenticationProvider() deve retornar um DaoAuthenticationProvider configurado")
    void shouldProvideAuthenticationProvider() {
        // Arrange
        UserDetailsService uds = Mockito.mock(UserDetailsService.class);
        SecurityConfig config = new SecurityConfig(uds);

        // Act
        AuthenticationProvider provider = config.authenticationProvider();

        // Assert
        assertThat(provider).isNotNull();
        // Verifica tipo esperado pelo nome da classe (DaoAuthenticationProvider)
        assertThat(provider.getClass().getSimpleName()).containsIgnoringCase("DaoAuthenticationProvider");
    }

    @Test
    @DisplayName("jwtAuthenticationFilter(...) deve instanciar JwtAuthenticationFilter")
    void shouldCreateJwtAuthenticationFilterBean() {
        // Arrange
        UserDetailsService uds = Mockito.mock(UserDetailsService.class);
        SecurityConfig config = new SecurityConfig(uds);

        JwtTokenUtil jwtTokenUtil = Mockito.mock(JwtTokenUtil.class);
        CustomUserDetailsService customUserDetailsService = Mockito.mock(CustomUserDetailsService.class);

        // Act
        JwtAuthenticationFilter filter = config.jwtAuthenticationFilter(jwtTokenUtil, customUserDetailsService);

        // Assert
        assertThat(filter).isNotNull();
    }
}