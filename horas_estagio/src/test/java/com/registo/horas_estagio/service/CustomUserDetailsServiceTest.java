package com.registo.horas_estagio.service;

import com.registo.horas_estagio.models.Usuario;
import com.registo.horas_estagio.repository.UsuarioRepository;
import com.registo.horas_estagio.service.impl.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService - Testes unitários essenciais")
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Usuario usuarioEstagiario;

    @BeforeEach
    void setUp() {
        usuarioEstagiario = Usuario.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .username("neto")
                .password("$2a$10$encodedPassword")
                .role("ROLE_ESTAGIARIO")
                .build();
    }

    @Test
    @DisplayName("loadUserByUsername deve retornar UserDetails quando usuário existe")
    void loadUserByUsername_returnsUserDetails_whenUserExists() {
        // Given
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuarioEstagiario));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("neto");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("neto");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$encodedPassword");

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities).extracting(GrantedAuthority::getAuthority).containsExactly("ROLE_ESTAGIARIO");

        verify(usuarioRepository).findByUsername("neto");
    }

    @Test
    @DisplayName("loadUserByUsername deve lançar UsernameNotFoundException quando usuário não existe")
    void loadUserByUsername_throwsWhenUserNotFound() {
        // Given
        when(usuarioRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("inexistente"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado: inexistente");

        verify(usuarioRepository).findByUsername("inexistente");
    }

    @Test
    @DisplayName("loadUserByUsername deve lançar UsernameNotFoundException quando role é nula ou em branco")
    void loadUserByUsername_throwsWhenRoleMissing() {
        // role nula
        Usuario usuarioSemRole = Usuario.builder()
                .id(2L)
                .publicId(UUID.randomUUID())
                .username("semrole")
                .password("$2a$10$encodedPassword")
                .role(null)
                .build();

        when(usuarioRepository.findByUsername("semrole")).thenReturn(Optional.of(usuarioSemRole));

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("semrole"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuário sem role definida: semrole");

        verify(usuarioRepository).findByUsername("semrole");

        // role em branco
        Usuario usuarioRoleVazia = Usuario.builder()
                .id(3L)
                .publicId(UUID.randomUUID())
                .username("rolevazia")
                .password("$2a$10$encodedPassword")
                .role("   ")
                .build();

        when(usuarioRepository.findByUsername("rolevazia")).thenReturn(Optional.of(usuarioRoleVazia));

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("rolevazia"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuário sem role definida: rolevazia");

        verify(usuarioRepository).findByUsername("rolevazia");
    }
}