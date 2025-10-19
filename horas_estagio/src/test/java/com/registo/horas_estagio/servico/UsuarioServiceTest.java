package com.registo.horas_estagio.servico;

import com.registo.horas_estagio.models.Usuario;
import com.registo.horas_estagio.repository.UsuarioRepository;
import com.registo.horas_estagio.service.impl.UsuarioServiceImpl;
import com.registo.horas_estagio.util.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do UsuarioService")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = ObjectUtils.createDefaultUsuario();
    }

    @Test
    @DisplayName("Deve registrar usuário com senha codificada")
    void shouldRegisterUserWithEncodedPassword() {
        // Given
        String senhaOriginal = "senha123";
        String senhaCodificada = "$2a$10$encodedPassword";

        Usuario novoUsuario = Usuario.builder()
                .username("maria")
                .password(senhaOriginal)
                .role("ROLE_ESTAGIARIO")
                .build();

        when(passwordEncoder.encode(senhaOriginal)).thenReturn(senhaCodificada);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        // When
        Usuario resultado = usuarioService.registrarUsuario(novoUsuario);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getUsername()).isEqualTo("maria");
        assertThat(resultado.getPassword()).isEqualTo(senhaCodificada);
        assertThat(resultado.getRole()).isEqualTo("ROLE_ESTAGIARIO");

        verify(passwordEncoder).encode(senhaOriginal);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve registrar usuário com role ADMIN")
    void shouldRegisterUserWithAdminRole() {
        // Given
        String senhaCodificada = "$2a$10$encodedPassword";

        Usuario adminUsuario = Usuario.builder()
                .username("admin")
                .password("admin123")
                .role("ROLE_ADMIN")
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn(senhaCodificada);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(adminUsuario);

        // When
        Usuario resultado = usuarioService.registrarUsuario(adminUsuario);

        // Then
        assertThat(resultado).isNotNull();
        assertThat(resultado.getUsername()).isEqualTo("admin");
        assertThat(resultado.getRole()).isEqualTo("ROLE_ADMIN");

        verify(passwordEncoder).encode("admin123");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve buscar usuário por username com sucesso")
    void shouldFindUserByUsernameSuccessfully() {
        // Given
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));

        // When
        Optional<Usuario> resultado = usuarioService.buscarPorUsername("neto");

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getUsername()).isEqualTo("neto");
        assertThat(resultado.get().getRole()).isEqualTo("ROLE_ESTAGIARIO");

        verify(usuarioRepository).findByUsername("neto");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando usuário não existe")
    void shouldReturnEmptyOptionalWhenUserNotFound() {
        // Given
        when(usuarioRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        // When
        Optional<Usuario> resultado = usuarioService.buscarPorUsername("inexistente");

        // Then
        assertThat(resultado).isEmpty();

        verify(usuarioRepository).findByUsername("inexistente");
    }

    @Test
    @DisplayName("Deve retornar true quando usuário existe")
    void shouldReturnTrueWhenUserExists() {
        // Given
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));

        // When
        boolean existe = usuarioService.existeUsuario("neto");

        // Then
        assertThat(existe).isTrue();

        verify(usuarioRepository).findByUsername("neto");
    }

    @Test
    @DisplayName("Deve retornar false quando usuário não existe")
    void shouldReturnFalseWhenUserDoesNotExist() {
        // Given
        when(usuarioRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        // When
        boolean existe = usuarioService.existeUsuario("inexistente");

        // Then
        assertThat(existe).isFalse();

        verify(usuarioRepository).findByUsername("inexistente");
    }

    @Test
    @DisplayName("Deve verificar que senha é sempre codificada no registro")
    void shouldAlwaysEncodePasswordOnRegister() {
        // Given
        String[] senhas = {"senha1", "senha2", "senha3"};

        for (String senha : senhas) {
            Usuario user = Usuario.builder()
                    .username("user_" + senha)
                    .password(senha)
                    .role("ROLE_ESTAGIARIO")
                    .build();

            when(passwordEncoder.encode(senha)).thenReturn("encoded_" + senha);
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(user);

            // When
            usuarioService.registrarUsuario(user);

            // Then
            verify(passwordEncoder).encode(senha);
        }

        verify(passwordEncoder, times(3)).encode(anyString());
        verify(usuarioRepository, times(3)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve manter username case-sensitive ao buscar")
    void shouldMaintainCaseSensitiveUsernameWhenSearching() {
        // Given
        Usuario usuarioMaiusculo = Usuario.builder()
                .username("NETO")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        when(usuarioRepository.findByUsername("NETO")).thenReturn(Optional.of(usuarioMaiusculo));
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));

        // When
        Optional<Usuario> resultadoMaiusculo = usuarioService.buscarPorUsername("NETO");
        Optional<Usuario> resultadoMinusculo = usuarioService.buscarPorUsername("neto");

        // Then
        assertThat(resultadoMaiusculo).isPresent();
        assertThat(resultadoMaiusculo.get().getUsername()).isEqualTo("NETO");

        assertThat(resultadoMinusculo).isPresent();
        assertThat(resultadoMinusculo.get().getUsername()).isEqualTo("neto");

        verify(usuarioRepository).findByUsername("NETO");
        verify(usuarioRepository).findByUsername("neto");
    }

    @Test
    @DisplayName("Deve chamar repository apenas uma vez por operação")
    void shouldCallRepositoryOnlyOncePerOperation() {
        // Given
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));

        // When
        usuarioService.buscarPorUsername("neto");

        // Then
        verify(usuarioRepository, times(1)).findByUsername("neto");
        verifyNoMoreInteractions(usuarioRepository);
    }

    @Test
    @DisplayName("Deve preservar todos os dados do usuário ao registrar")
    void shouldPreserveAllUserDataWhenRegistering() {
        // Given
        Usuario usuarioCompleto = Usuario.builder()
                .username("joao")
                .password("senha456")
                .role("ROLE_ADMIN")
                .build();

        when(passwordEncoder.encode("senha456")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        // When
        Usuario resultado = usuarioService.registrarUsuario(usuarioCompleto);

        // Then
        assertThat(resultado.getUsername()).isEqualTo("joao");
        assertThat(resultado.getPassword()).isEqualTo("encodedPassword");
        assertThat(resultado.getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(resultado.getId()).isNotNull();

        verify(usuarioRepository).save(argThat(user ->
                user.getUsername().equals("joao") &&
                        user.getPassword().equals("encodedPassword") &&
                        user.getRole().equals("ROLE_ADMIN")
        ));
    }

    @Test
    @DisplayName("Deve permitir verificar existência múltiplas vezes")
    void shouldAllowMultipleExistenceChecks() {
        // Given
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        // When
        boolean existe1 = usuarioService.existeUsuario("neto");
        boolean existe2 = usuarioService.existeUsuario("inexistente");
        boolean existe3 = usuarioService.existeUsuario("neto");

        // Then
        assertThat(existe1).isTrue();
        assertThat(existe2).isFalse();
        assertThat(existe3).isTrue();

        verify(usuarioRepository, times(2)).findByUsername("neto");
        verify(usuarioRepository, times(1)).findByUsername("inexistente");
    }
}