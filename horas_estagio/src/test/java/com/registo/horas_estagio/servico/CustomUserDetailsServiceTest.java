package com.registo.horas_estagio.servico;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do CustomUserDetailsService")
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private Usuario usuarioEstagiario;
    private Usuario usuarioAdmin;

    @BeforeEach
    void setUp() {
        usuarioEstagiario = Usuario.builder()
                .id(UUID.randomUUID())
                .username("neto")
                .password("$2a$10$encodedPassword")
                .role("ROLE_ESTAGIARIO")
                .build();

        usuarioAdmin = Usuario.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .password("$2a$10$encodedAdminPassword")
                .role("ROLE_ADMIN")
                .build();
    }

    @Test
    @DisplayName("Deve carregar usuário por username com sucesso")
    void shouldLoadUserByUsernameSuccessfully() {
        // Given
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuarioEstagiario));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("neto");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("neto");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$encodedPassword");
        assertThat(userDetails.getAuthorities()).isNotEmpty();

        verify(usuarioRepository).findByUsername("neto");
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando usuário não existe")
    void shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        // Given
        when(usuarioRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("inexistente"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado: inexistente");

        verify(usuarioRepository).findByUsername("inexistente");
    }

    @Test
    @DisplayName("Deve carregar usuário ADMIN com authorities corretas")
    void shouldLoadAdminUserWithCorrectAuthorities() {
        // Given
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioAdmin));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getAuthorities()).hasSize(1);

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");

        verify(usuarioRepository).findByUsername("admin");
    }

    @Test
    @DisplayName("Deve carregar usuário ESTAGIARIO com authorities corretas")
    void shouldLoadEstagiarioUserWithCorrectAuthorities() {
        // Given
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuarioEstagiario));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("neto");

        // Then
        assertThat(userDetails).isNotNull();
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertThat(authorities).hasSize(1);
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ESTAGIARIO");

        verify(usuarioRepository).findByUsername("neto");
    }

    @Test
    @DisplayName("Deve adicionar prefixo ROLE_ quando não existe")
    void shouldAddRolePrefixWhenMissing() {
        // Given
        Usuario usuarioSemPrefixo = Usuario.builder()
                .id(UUID.randomUUID())
                .username("joao")
                .password("$2a$10$encodedPassword")
                .role("ESTAGIARIO") // Sem prefixo ROLE_
                .build();

        when(usuarioRepository.findByUsername("joao")).thenReturn(Optional.of(usuarioSemPrefixo));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("joao");

        // Then
        assertThat(userDetails).isNotNull();
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ESTAGIARIO");

        verify(usuarioRepository).findByUsername("joao");
    }

    @Test
    @DisplayName("Deve manter prefixo ROLE_ quando já existe")
    void shouldKeepRolePrefixWhenAlreadyExists() {
        // Given
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioAdmin));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin");

        // Then
        assertThat(userDetails).isNotNull();
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");

        verify(usuarioRepository).findByUsername("admin");
    }

    @Test
    @DisplayName("Deve carregar usuário com role em minúscula e converter para maiúscula")
    void shouldConvertLowercaseRoleToUppercase() {
        // Given
        Usuario usuarioMinuscula = Usuario.builder()
                .id(UUID.randomUUID())
                .username("maria")
                .password("$2a$10$encodedPassword")
                .role("estagiario") // Role em minúscula sem prefixo
                .build();

        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(usuarioMinuscula));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("maria");

        // Then
        assertThat(userDetails).isNotNull();
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ESTAGIARIO");

        verify(usuarioRepository).findByUsername("maria");
    }

    @Test
    @DisplayName("Deve preservar senha criptografada do usuário")
    void shouldPreserveEncryptedPassword() {
        // Given
        String senhaCriptografada = "$2a$10$specificEncodedPassword123";
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .username("pedro")
                .password(senhaCriptografada)
                .role("ROLE_ESTAGIARIO")
                .build();

        when(usuarioRepository.findByUsername("pedro")).thenReturn(Optional.of(usuario));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("pedro");

        // Then
        assertThat(userDetails.getPassword()).isEqualTo(senhaCriptografada);

        verify(usuarioRepository).findByUsername("pedro");
    }

    @Test
    @DisplayName("Deve retornar UserDetails com username exato do banco")
    void shouldReturnUserDetailsWithExactUsername() {
        // Given
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuarioEstagiario));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("neto");

        // Then
        assertThat(userDetails.getUsername()).isEqualTo(usuarioEstagiario.getUsername());
        assertThat(userDetails.getUsername()).isEqualTo("neto");

        verify(usuarioRepository).findByUsername("neto");
    }

    @Test
    @DisplayName("Deve chamar repository apenas uma vez por carregamento")
    void shouldCallRepositoryOnlyOncePerLoad() {
        // Given
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuarioEstagiario));

        // When
        customUserDetailsService.loadUserByUsername("neto");

        // Then
        verify(usuarioRepository, times(1)).findByUsername("neto");
        verifyNoMoreInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("Deve ser case-sensitive ao buscar username")
    void shouldBeCaseSensitiveWhenSearchingUsername() {
        // Given
        Usuario usuarioMaiusculo = Usuario.builder()
                .id(UUID.randomUUID())
                .username("NETO")
                .password("$2a$10$encodedPassword")
                .role("ROLE_ESTAGIARIO")
                .build();

        when(usuarioRepository.findByUsername("NETO")).thenReturn(Optional.of(usuarioMaiusculo));
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuarioEstagiario));

        // When
        UserDetails userDetailsMaiusculo = customUserDetailsService.loadUserByUsername("NETO");
        UserDetails userDetailsMinusculo = customUserDetailsService.loadUserByUsername("neto");

        // Then
        assertThat(userDetailsMaiusculo.getUsername()).isEqualTo("NETO");
        assertThat(userDetailsMinusculo.getUsername()).isEqualTo("neto");

        verify(usuarioRepository).findByUsername("NETO");
        verify(usuarioRepository).findByUsername("neto");
    }

    @Test
    @DisplayName("Deve retornar UserDetails com conta habilitada por padrão")
    void shouldReturnUserDetailsWithEnabledAccount() {
        // Given
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuarioEstagiario));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("neto");

        // Then
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();

        verify(usuarioRepository).findByUsername("neto");
    }

    @Test
    @DisplayName("Deve processar múltiplos carregamentos de diferentes usuários")
    void shouldProcessMultipleLoadsOfDifferentUsers() {
        // Given
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuarioEstagiario));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioAdmin));

        // When
        UserDetails userDetails1 = customUserDetailsService.loadUserByUsername("neto");
        UserDetails userDetails2 = customUserDetailsService.loadUserByUsername("admin");

        // Then
        assertThat(userDetails1.getUsername()).isEqualTo("neto");
        assertThat(userDetails2.getUsername()).isEqualTo("admin");

        assertThat(userDetails1.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ESTAGIARIO");

        assertThat(userDetails2.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");

        verify(usuarioRepository).findByUsername("neto");
        verify(usuarioRepository).findByUsername("admin");
    }

    @Test
    @DisplayName("Deve lançar exceção com mensagem específica para cada usuário não encontrado")
    void shouldThrowExceptionWithSpecificMessageForEachNotFoundUser() {
        // Given
        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.empty());
        when(usuarioRepository.findByUsername("user2")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("user1"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("user1");

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("user2"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("user2");

        verify(usuarioRepository).findByUsername("user1");
        verify(usuarioRepository).findByUsername("user2");
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando role é null")
    void shouldThrowIllegalStateExceptionWhenRoleIsNull() {
        // Given
        Usuario usuarioSemRole = Usuario.builder()
                .id(UUID.randomUUID())
                .username("semrole")
                .password("$2a$10$encodedPassword")
                .role(null) // Isso não deveria acontecer devido ao nullable=false
                .build();

        when(usuarioRepository.findByUsername("semrole")).thenReturn(Optional.of(usuarioSemRole));

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("semrole"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Usuário sem role definida: semrole");

        verify(usuarioRepository).findByUsername("semrole");
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando role é vazia")
    void shouldThrowIllegalStateExceptionWhenRoleIsBlank() {
        // Given
        Usuario usuarioRoleVazia = Usuario.builder()
                .id(UUID.randomUUID())
                .username("rolevazia")
                .password("$2a$10$encodedPassword")
                .role("   ") // Role vazia/em branco
                .build();

        when(usuarioRepository.findByUsername("rolevazia")).thenReturn(Optional.of(usuarioRoleVazia));

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("rolevazia"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Usuário sem role definida: rolevazia");

        verify(usuarioRepository).findByUsername("rolevazia");
    }
}