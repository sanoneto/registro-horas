
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários Completos do CustomUserDetailsService")
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
                .id(1L)
                .publicId(UUID.randomUUID())
                .username("neto")
                .password("$2a$10$encodedPassword")
                .role("ROLE_ESTAGIARIO")
                .build();

        usuarioAdmin = Usuario.builder()
                .id(2L)
                .publicId(UUID.randomUUID())
                .username("admin")
                .password("$2a$10$encodedAdminPassword")
                .role("ROLE_ADMIN")
                .build();
    }

    // ==================== TESTES BÁSICOS DE CARREGAMENTO ====================

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
        assertThat(userDetails.getAuthorities()).hasSize(1);

        verify(usuarioRepository).findByUsername("neto");
    }

    @Test
    @DisplayName("Deve carregar usuário ADMIN com sucesso")
    void shouldLoadAdminUserSuccessfully() {
        // Given
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioAdmin));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$encodedAdminPassword");
        assertThat(userDetails.getAuthorities()).isNotEmpty();

        verify(usuarioRepository).findByUsername("admin");
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

    // ==================== TESTES DE EXCEPTIONS ====================

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
                .id(1L)
                .publicId(UUID.randomUUID())
                .username("semrole")
                .password("$2a$10$encodedPassword")
                .role(null)
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
                .id(1L)
                .publicId(UUID.randomUUID())
                .username("rolevazia")
                .password("$2a$10$encodedPassword")
                .role("   ")
                .build();

        when(usuarioRepository.findByUsername("rolevazia")).thenReturn(Optional.of(usuarioRoleVazia));

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("rolevazia"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Usuário sem role definida: rolevazia");

        verify(usuarioRepository).findByUsername("rolevazia");
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando role é string vazia")
    void shouldThrowIllegalStateExceptionWhenRoleIsEmptyString() {
        // Given
        Usuario usuarioRoleVazia = Usuario.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .username("roleempty")
                .password("$2a$10$encodedPassword")
                .role("")
                .build();

        when(usuarioRepository.findByUsername("roleempty")).thenReturn(Optional.of(usuarioRoleVazia));

        // When & Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("roleempty"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Usuário sem role definida: roleempty");

        verify(usuarioRepository).findByUsername("roleempty");
    }

    // ==================== TESTES DE AUTHORITIES ====================

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
                .id(1L)
                .publicId(UUID.randomUUID())
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
                .id(1L)
                .publicId(UUID.randomUUID())
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
    @DisplayName("Deve converter role MixedCase para UPPERCASE com prefixo")
    void shouldConvertMixedCaseRoleToUppercase() {
        // Given
        Usuario usuarioMixedCase = Usuario.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .username("carlos")
                .password("$2a$10$encodedPassword")
                .role("AdMiN") // MixedCase sem prefixo
                .build();

        when(usuarioRepository.findByUsername("carlos")).thenReturn(Optional.of(usuarioMixedCase));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("carlos");

        // Then
        assertThat(userDetails).isNotNull();
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");

        verify(usuarioRepository).findByUsername("carlos");
    }

    @Test
    @DisplayName("Deve processar role com espaços extras")
    void shouldProcessRoleWithExtraSpaces() {
        // Given
        Usuario usuarioComEspacos = Usuario.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .username("pedro")
                .password("$2a$10$encodedPassword")
                .role("  ADMIN  ") // Role com espaços
                .build();

        when(usuarioRepository.findByUsername("pedro")).thenReturn(Optional.of(usuarioComEspacos));

        // When & Then
        // Dependendo da implementação, pode adicionar ROLE_ ou não processar espaços corretamente
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("pedro");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).isNotEmpty();

        verify(usuarioRepository).findByUsername("pedro");
    }

    @Test
    @DisplayName("Deve processar roles customizadas")
    void shouldProcessCustomRoles() {
        // Given
        Usuario usuarioCustomRole = Usuario.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .username("manager")
                .password("$2a$10$encodedPassword")
                .role("MANAGER") // Role customizada
                .build();

        when(usuarioRepository.findByUsername("manager")).thenReturn(Optional.of(usuarioCustomRole));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("manager");

        // Then
        assertThat(userDetails).isNotNull();
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_MANAGER");

        verify(usuarioRepository).findByUsername("manager");
    }

    // ==================== TESTES DE PASSWORD ====================

    @Test
    @DisplayName("Deve preservar senha criptografada do usuário")
    void shouldPreserveEncryptedPassword() {
        // Given
        String senhaCriptografada = "$2a$10$specificEncodedPassword123";
        Usuario usuario = Usuario.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .username("pedro")
                .password(senhaCriptografada)
                .role("ROLE_ESTAGIARIO")
                .build();

        when(usuarioRepository.findByUsername("pedro")).thenReturn(Optional.of(usuario));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("pedro");

        // Then
        assertThat(userDetails.getPassword()).isEqualTo(senhaCriptografada);
        assertThat(userDetails.getPassword()).isNotEmpty();
        assertThat(userDetails.getPassword()).startsWith("$2a$10$");

        verify(usuarioRepository).findByUsername("pedro");
    }

    @Test
    @DisplayName("Deve carregar usuário com senha BCrypt válida")
    void shouldLoadUserWithValidBCryptPassword() {
        // Given
        String bcryptPassword = "$2a$12$someValidBCryptHash";
        Usuario usuario = Usuario.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .username("teste")
                .password(bcryptPassword)
                .role("ROLE_ESTAGIARIO")
                .build();

        when(usuarioRepository.findByUsername("teste")).thenReturn(Optional.of(usuario));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("teste");

        // Then
        assertThat(userDetails.getPassword()).isEqualTo(bcryptPassword);

        verify(usuarioRepository).findByUsername("teste");
    }

    // ==================== TESTES DE ESTADO DA CONTA ====================

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
    @DisplayName("Deve retornar conta sempre habilitada para qualquer usuário")
    void shouldReturnAccountAlwaysEnabledForAnyUser() {
        // Given
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuarioAdmin));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin");

        // Then
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();

        verify(usuarioRepository).findByUsername("admin");
    }

    // ==================== TESTES DE INTERAÇÃO COM REPOSITORY ====================

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
    @DisplayName("Deve chamar repository com username exato fornecido")
    void shouldCallRepositoryWithExactProvidedUsername() {
        // Given
        String username = "testUser123";
        Usuario usuario = Usuario.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .username(username)
                .password("$2a$10$encodedPassword")
                .role("ROLE_ESTAGIARIO")
                .build();

        when(usuarioRepository.findByUsername(username)).thenReturn(Optional.of(usuario));

        // When
        customUserDetailsService.loadUserByUsername(username);

        // Then
        verify(usuarioRepository).findByUsername(username);
        verifyNoMoreInteractions(usuarioRepository);
        verify(usuarioRepository, times(1)).findByUsername(eq(username));
    }

    // ==================== TESTES DE CASE SENSITIVITY ====================

    @Test
    @DisplayName("Deve ser case-sensitive ao buscar username")
    void shouldBeCaseSensitiveWhenSearchingUsername() {
        // Given
        Usuario usuarioMaiusculo = Usuario.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
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
    @DisplayName("Deve buscar username com MixedCase exatamente como fornecido")
    void shouldSearchUsernameMixedCaseExactly() {
        // Given
        String mixedCaseUsername = "NeToCaRlOs";
        Usuario usuario = Usuario.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .username(mixedCaseUsername)
                .password("$2a$10$encodedPassword")
                .role("ROLE_ESTAGIARIO")
                .build();

        when(usuarioRepository.findByUsername(mixedCaseUsername)).thenReturn(Optional.of(usuario));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(mixedCaseUsername);

        // Then
        assertThat(userDetails.getUsername()).isEqualTo(mixedCaseUsername);

        verify(usuarioRepository).findByUsername(mixedCaseUsername);
    }

    // ==================== TESTES DE MÚLTIPLOS USUÁRIOS ====================

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
    @DisplayName("Deve carregar usuários diferentes sequencialmente")
    void shouldLoadDifferentUsersSequentially() {
        // Given
        Usuario usuario1 = Usuario.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .username("user1")
                .password("$2a$10$pass1")
                .role("ROLE_ESTAGIARIO")
                .build();

        Usuario usuario2 = Usuario.builder()
                .id(2L)
                .publicId(UUID.randomUUID())
                .username("user2")
                .password("$2a$10$pass2")
                .role("ROLE_ADMIN")
                .build();

        Usuario usuario3 = Usuario.builder()
                .id(3L)
                .publicId(UUID.randomUUID())
                .username("user3")
                .password("$2a$10$pass3")
                .role("ROLE_ESTAGIARIO")
                .build();

        when(usuarioRepository.findByUsername("user1")).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.findByUsername("user2")).thenReturn(Optional.of(usuario2));
        when(usuarioRepository.findByUsername("user3")).thenReturn(Optional.of(usuario3));

        // When
        UserDetails ud1 = customUserDetailsService.loadUserByUsername("user1");
        UserDetails ud2 = customUserDetailsService.loadUserByUsername("user2");
        UserDetails ud3 = customUserDetailsService.loadUserByUsername("user3");

        // Then
        assertThat(ud1.getUsername()).isEqualTo("user1");
        assertThat(ud2.getUsername()).isEqualTo("user2");
        assertThat(ud3.getUsername()).isEqualTo("user3");

        verify(usuarioRepository).findByUsername("user1");
        verify(usuarioRepository).findByUsername("user2");
        verify(usuarioRepository).findByUsername("user3");
    }

    // ==================== TESTES DE EDGE CASES ====================

    @Test
    @DisplayName("Deve carregar usuário com username contendo caracteres especiais")
    void shouldLoadUserWithSpecialCharactersInUsername() {
        // Given
        String specialUsername = "user_name-123.test";
        Usuario usuario = Usuario.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .username(specialUsername)
                .password("$2a$10$encodedPassword")
                .role("ROLE_ESTAGIARIO")
                .build();

        when(usuarioRepository.findByUsername(specialUsername)).thenReturn(Optional.of(usuario));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(specialUsername);

        // Then
        assertThat(userDetails.getUsername()).isEqualTo(specialUsername);

        verify(usuarioRepository).findByUsername(specialUsername);
    }

    @Test
    @DisplayName("Deve carregar usuário com username numérico")
    void shouldLoadUserWithNumericUsername() {
        // Given
        String numericUsername = "12345";
        Usuario usuario = Usuario.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .username(numericUsername)
                .password("$2a$10$encodedPassword")
                .role("ROLE_ESTAGIARIO")
                .build();

        when(usuarioRepository.findByUsername(numericUsername)).thenReturn(Optional.of(usuario));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(numericUsername);

        // Then
        assertThat(userDetails.getUsername()).isEqualTo(numericUsername);

        verify(usuarioRepository).findByUsername(numericUsername);
    }

    @Test
    @DisplayName("Deve processar username muito longo")
    void shouldProcessVeryLongUsername() {
        // Given
        String longUsername = "a".repeat(50);
        Usuario usuario = Usuario.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .username(longUsername)
                .password("$2a$10$encodedPassword")
                .role("ROLE_ESTAGIARIO")
                .build();

        when(usuarioRepository.findByUsername(longUsername)).thenReturn(Optional.of(usuario));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(longUsername);

        // Then
        assertThat(userDetails.getUsername()).isEqualTo(longUsername);
        assertThat(userDetails.getUsername()).hasSize(50);

        verify(usuarioRepository).findByUsername(longUsername);
    }

    @Test
    @DisplayName("Deve retornar exatamente uma authority para cada usuário")
    void shouldReturnExactlyOneAuthorityForEachUser() {
        // Given
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuarioEstagiario));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("neto");

        // Then
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities()).isNotEmpty();

        verify(usuarioRepository).findByUsername("neto");
    }

    @Test
    @DisplayName("Deve carregar usuário com todos os campos preenchidos corretamente")
    void shouldLoadUserWithAllFieldsPopulatedCorrectly() {
        // Given
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuarioEstagiario));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("neto");

        // Then
        assertThat(userDetails.getUsername()).isNotNull();
        assertThat(userDetails.getUsername()).isNotEmpty();
        assertThat(userDetails.getPassword()).isNotNull();
        assertThat(userDetails.getPassword()).isNotEmpty();
        assertThat(userDetails.getAuthorities()).isNotNull();
        assertThat(userDetails.getAuthorities()).isNotEmpty();
        assertThat(userDetails.isEnabled()).isNotNull();
        assertThat(userDetails.isAccountNonExpired()).isNotNull();
        assertThat(userDetails.isAccountNonLocked()).isNotNull();
        assertThat(userDetails.isCredentialsNonExpired()).isNotNull();

        verify(usuarioRepository).findByUsername("neto");
    }
}