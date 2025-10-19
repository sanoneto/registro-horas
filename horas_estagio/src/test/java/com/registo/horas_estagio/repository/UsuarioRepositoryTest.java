package com.registo.horas_estagio.repository;

import com.registo.horas_estagio.models.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes do UsuarioRepository")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Usuario usuario1;
    private Usuario usuario2;

    @BeforeEach
    void setUp() {
        // Limpar dados existentes
        usuarioRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Criar usuários de teste
        usuario1 = Usuario.builder()
                .username("neto")
                .password("senha123")
                .role("ROLE_ESTAGIARIO")
                .build();

        usuario2 = Usuario.builder()
                .username("admin")
                .password("admin123")
                .role("ROLE_ADMIN")
                .build();

        usuarioRepository.saveAll(List.of(usuario1, usuario2));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Deve salvar usuário com sucesso")
    void shouldSaveUsuario() {
        // Given
        Usuario novoUsuario = Usuario.builder()
                .username("maria")
                .password("maria123")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When
        Usuario saved = usuarioRepository.save(novoUsuario);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUsername()).isEqualTo("maria");
        assertThat(saved.getPassword()).isEqualTo("maria123");
        assertThat(saved.getRole()).isEqualTo("ROLE_ESTAGIARIO");
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void shouldFindUsuarioById() {
        // When
        Optional<Usuario> found = usuarioRepository.findById(usuario1.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("neto");
    }

    @Test
    @DisplayName("Deve retornar vazio quando ID não existe")
    void shouldReturnEmptyWhenIdNotFound() {
        // Given
        UUID idInexistente = UUID.randomUUID();

        // When
        Optional<Usuario> found = usuarioRepository.findById(idInexistente);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar usuário por username")
    void shouldFindByUsername() {
        // When
        Optional<Usuario> found = usuarioRepository.findByUsername("neto");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("neto");
        assertThat(found.get().getRole()).isEqualTo("ROLE_ESTAGIARIO");
    }

    @Test
    @DisplayName("Deve retornar vazio quando username não existe")
    void shouldReturnEmptyWhenUsernameNotFound() {
        // When
        Optional<Usuario> found = usuarioRepository.findByUsername("inexistente");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar todos os usuários")
    void shouldFindAllUsuarios() {
        // When
        List<Usuario> usuarios = usuarioRepository.findAll();

        // Then
        assertThat(usuarios).isNotEmpty();
        assertThat(usuarios).hasSize(2);
        assertThat(usuarios).extracting(Usuario::getUsername)
                .containsExactlyInAnyOrder("neto", "admin");
    }

    @Test
    @DisplayName("Deve atualizar usuário existente")
    void shouldUpdateUsuario() {
        // Given
        Usuario usuarioExistente = usuarioRepository.findByUsername("neto")
                .orElseThrow(() -> new AssertionError("Usuário deveria existir"));
        usuarioExistente.setPassword("novaSenha456");
        usuarioExistente.setRole("ROLE_ADMIN");

        // When
        Usuario updated = usuarioRepository.save(usuarioExistente);

        // Then
        assertThat(updated.getId()).isEqualTo(usuario1.getId());
        assertThat(updated.getUsername()).isEqualTo("neto");
        assertThat(updated.getPassword()).isEqualTo("novaSenha456");
        assertThat(updated.getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Deve deletar usuário por ID")
    void shouldDeleteUsuarioById() {
        // Given
        UUID idParaDeletar = usuario1.getId();

        // When
        usuarioRepository.deleteById(idParaDeletar);

        // Then
        Optional<Usuario> deleted = usuarioRepository.findById(idParaDeletar);
        assertThat(deleted).isEmpty();

        List<Usuario> remaining = usuarioRepository.findAll();
        assertThat(remaining).hasSize(1);
    }

    @Test
    @DisplayName("Deve deletar usuário por entidade")
    void shouldDeleteUsuarioByEntity() {
        // When
        usuarioRepository.delete(usuario2);

        // Then
        Optional<Usuario> deleted = usuarioRepository.findByUsername("admin");
        assertThat(deleted).isEmpty();

        List<Usuario> remaining = usuarioRepository.findAll();
        assertThat(remaining).hasSize(1);
    }

    @Test
    @DisplayName("Deve contar total de usuários")
    void shouldCountTotalUsuarios() {
        // When
        long count = usuarioRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve verificar se usuário existe por ID")
    void shouldCheckIfUsuarioExistsById() {
        // When
        boolean exists = usuarioRepository.existsById(usuario1.getId());
        boolean notExists = usuarioRepository.existsById(UUID.randomUUID());

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Não deve permitir username duplicado")
    void shouldNotAllowDuplicateUsername() {
        // Given
        Usuario usuarioDuplicado = Usuario.builder()
                .username("neto")  // Username já existe
                .password("outraSenha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When & Then
        assertThatThrownBy(() -> usuarioRepository.saveAndFlush(usuarioDuplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve permitir usuários com usernames diferentes")
    void shouldAllowDifferentUsernames() {
        // Given
        Usuario usuario3 = Usuario.builder()
                .username("joao")
                .password("senha789")
                .role("ROLE_ESTAGIARIO")
                .build();

        Usuario usuario4 = Usuario.builder()
                .username("pedro")
                .password("senha101")
                .role("ROLE_ADMIN")
                .build();

        // When
        usuarioRepository.saveAll(List.of(usuario3, usuario4));

        // Then
        List<Usuario> usuarios = usuarioRepository.findAll();
        assertThat(usuarios).hasSize(4);
    }

    @Test
    @DisplayName("Username deve ser case-sensitive")
    void usernameShouldBeCaseSensitive() {
        // Given
        Usuario usuarioMaiusculo = Usuario.builder()
                .username("NETO")  // Maiúsculo
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When
        Usuario saved = usuarioRepository.save(usuarioMaiusculo);

        // Then
        assertThat(usuarioRepository.findByUsername("NETO")).isPresent();
        assertThat(usuarioRepository.findByUsername("neto")).isPresent();
        assertThat(saved.getUsername()).isEqualTo("NETO");
    }

    @Test
    @DisplayName("Deve deletar todos os usuários")
    void shouldDeleteAllUsuarios() {
        // When
        usuarioRepository.deleteAll();

        // Then
        assertThat(usuarioRepository.count()).isZero();
        assertThat(usuarioRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar se username existe")
    void shouldCheckIfUsernameExists() {
        // When
        boolean existsNeto = usuarioRepository.findByUsername("neto").isPresent();
        boolean existsInexistente = usuarioRepository.findByUsername("inexistente").isPresent();

        // Then
        assertThat(existsNeto).isTrue();
        assertThat(existsInexistente).isFalse();
    }

    @Test
    @DisplayName("Deve salvar usuário com lista de registros vazia")
    void shouldSaveUsuarioWithEmptyRegistrosList() {
        // Given
        Usuario usuario = Usuario.builder()
                .username("carlos")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .registros(new ArrayList<>())
                .build();

        // When
        Usuario saved = usuarioRepository.save(usuario);

        // Then
        assertThat(saved.getRegistros()).isNotNull();
        assertThat(saved.getRegistros()).isEmpty();
    }

    @Test
    @DisplayName("Deve manter integridade ao deletar em lote")
    void shouldMaintainIntegrityWhenBatchDeleting() {
        // Given
        List<Usuario> usuariosParaDeletar = List.of(usuario1);

        // When
        usuarioRepository.deleteAll(usuariosParaDeletar);
        entityManager.flush();

        // Then
        assertThat(usuarioRepository.count()).isEqualTo(1);
        assertThat(usuarioRepository.findByUsername("neto")).isEmpty();
        assertThat(usuarioRepository.findByUsername("admin")).isPresent();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há usuários")
    void shouldReturnEmptyListWhenNoUsuarios() {
        // Given
        usuarioRepository.deleteAll();
        entityManager.flush();

        // When
        List<Usuario> usuarios = usuarioRepository.findAll();

        // Then
        assertThat(usuarios).isEmpty();
        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    @DisplayName("Deve salvar e recuperar usuário com caracteres especiais no username")
    void shouldSaveAndRetrieveUsuarioWithSpecialCharactersInUsername() {
        // Given
        Usuario usuario = Usuario.builder()
                .username("user.name_123-test")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When
        Usuario saved = usuarioRepository.save(usuario);
        Optional<Usuario> found = usuarioRepository.findByUsername("user.name_123-test");

        // Then
        assertThat(saved.getUsername()).isEqualTo("user.name_123-test");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("user.name_123-test");
    }

    @Test
    @DisplayName("Não deve permitir salvar usuário sem username")
    void shouldNotAllowSavingUsuarioWithoutUsername() {
        // Given
        Usuario usuarioSemUsername = Usuario.builder()
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When & Then
        assertThatThrownBy(() -> {
            usuarioRepository.save(usuarioSemUsername);
            entityManager.flush();
        }).isInstanceOf(Exception.class); // Pode ser DataIntegrityViolationException ou ConstraintViolationException
    }

    @Test
    @DisplayName("Não deve permitir salvar usuário sem password")
    void shouldNotAllowSavingUsuarioWithoutPassword() {
        // Given
        Usuario usuarioSemPassword = Usuario.builder()
                .username("sempassword")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When & Then
        assertThatThrownBy(() -> {
            usuarioRepository.save(usuarioSemPassword);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Não deve permitir salvar usuário sem role")
    void shouldNotAllowSavingUsuarioWithoutRole() {
        // Given
        Usuario usuarioSemRole = Usuario.builder()
                .username("semrole")
                .password("senha")
                .build();

        // When & Then
        assertThatThrownBy(() -> {
            usuarioRepository.save(usuarioSemRole);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Deve atualizar apenas a senha do usuário")
    void shouldUpdateOnlyPassword() {
        // Given
        Usuario usuario = usuarioRepository.findByUsername("neto")
                .orElseThrow(() -> new AssertionError("Usuário deveria existir"));
        String senhaOriginal = usuario.getPassword();
        String roleOriginal = usuario.getRole();

        // When
        usuario.setPassword("novaSenha999");
        Usuario updated = usuarioRepository.save(usuario);

        // Then
        assertThat(updated.getPassword()).isEqualTo("novaSenha999");
        assertThat(updated.getPassword()).isNotEqualTo(senhaOriginal);
        assertThat(updated.getRole()).isEqualTo(roleOriginal);
        assertThat(updated.getUsername()).isEqualTo("neto");
    }

    @Test
    @DisplayName("Deve atualizar apenas a role do usuário")
    void shouldUpdateOnlyRole() {
        // Given
        Usuario usuario = usuarioRepository.findByUsername("neto")
                .orElseThrow(() -> new AssertionError("Usuário deveria existir"));
        String senhaOriginal = usuario.getPassword();

        // When
        usuario.setRole("ROLE_MANAGER");
        Usuario updated = usuarioRepository.save(usuario);

        // Then
        assertThat(updated.getRole()).isEqualTo("ROLE_MANAGER");
        assertThat(updated.getPassword()).isEqualTo(senhaOriginal);
        assertThat(updated.getUsername()).isEqualTo("neto");
    }

    @Test
    @DisplayName("Deve buscar múltiplos usuários e validar todos os campos")
    void shouldFindMultipleUsuariosAndValidateAllFields() {
        // When
        List<Usuario> usuarios = usuarioRepository.findAll();

        // Then
        assertThat(usuarios).hasSize(2);

        Usuario neto = usuarios.stream()
                .filter(u -> u.getUsername().equals("neto"))
                .findFirst()
                .orElseThrow();

        Usuario admin = usuarios.stream()
                .filter(u -> u.getUsername().equals("admin"))
                .findFirst()
                .orElseThrow();

        assertThat(neto.getId()).isNotNull();
        assertThat(neto.getPassword()).isEqualTo("senha123");
        assertThat(neto.getRole()).isEqualTo("ROLE_ESTAGIARIO");

        assertThat(admin.getId()).isNotNull();
        assertThat(admin.getPassword()).isEqualTo("admin123");
        assertThat(admin.getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Deve verificar que IDs são únicos e gerados automaticamente")
    void shouldVerifyIdsAreUniqueAndAutoGenerated() {
        // Given
        Usuario usuario3 = Usuario.builder()
                .username("teste1")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        Usuario usuario4 = Usuario.builder()
                .username("teste2")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When
        Usuario saved1 = usuarioRepository.save(usuario3);
        Usuario saved2 = usuarioRepository.save(usuario4);

        // Then
        assertThat(saved1.getId()).isNotNull();
        assertThat(saved2.getId()).isNotNull();
        assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
    }

    @Test
    @DisplayName("Deve manter consistência após flush e clear")
    void shouldMaintainConsistencyAfterFlushAndClear() {
        // Given
        String username = "consistencia";
        Usuario usuario = Usuario.builder()
                .username(username)
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When
        usuarioRepository.save(usuario);
        entityManager.flush();
        entityManager.clear();

        Optional<Usuario> found = usuarioRepository.findByUsername(username);

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo(username);
    }
}
