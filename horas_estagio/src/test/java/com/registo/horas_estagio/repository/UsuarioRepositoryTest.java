package com.registo.horas_estagio.repository;

import com.registo.horas_estagio.models.RegisterHoras;
import com.registo.horas_estagio.models.Usuario;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes Unitários Completos do UsuarioRepository")
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

    // ==================== TESTES BÁSICOS DE CRUD ====================

    @Test
    @DisplayName("Deve salvar usuário com sucesso")
    void shouldSaveUsuarioSuccessfully() {
        // Given
        Usuario novoUsuario = Usuario.builder()
                .username("maria")
                .password("maria123")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When
        Usuario saved = usuarioRepository.save(novoUsuario);
        entityManager.flush();
        entityManager.clear(); // ⭐ Limpa o cache do EntityManager

        // Then
        Usuario found = usuarioRepository.findById(saved.getId()).orElseThrow();

        assertThat(found).isNotNull();
        assertThat(found.getId()).isNotNull();
        assertThat(found.getPublicId()).isNotNull();
        assertThat(found.getUsername()).isEqualTo("maria");
        assertThat(found.getPassword()).isEqualTo("maria123");
        assertThat(found.getRole()).isEqualTo("ROLE_ESTAGIARIO");
        assertThat(found.getRegistros()).isNotNull();
        assertThat(found.getRegistros()).isEmpty(); // ⭐ Valida que é lista vazia
    }

    @Test
    @DisplayName("Deve gerar publicId automaticamente ao salvar")
    void shouldGeneratePublicIdAutomatically() {
        // Given
        Usuario usuario = Usuario.builder()
                .username("testpublicid")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When
        Usuario saved = usuarioRepository.save(usuario);
        entityManager.flush();

        // Then
        assertThat(saved.getPublicId()).isNotNull();
        assertThat(saved.getPublicId()).isInstanceOf(UUID.class);
    }

    @Test
    @DisplayName("Deve garantir unicidade do publicId")
    void shouldEnsurePublicIdUniqueness() {
        // Given
        Usuario usuario1 = Usuario.builder()
                .username("user1")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        Usuario usuario2 = Usuario.builder()
                .username("user2")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When
        Usuario saved1 = usuarioRepository.save(usuario1);
        Usuario saved2 = usuarioRepository.save(usuario2);
        entityManager.flush();

        // Then
        assertThat(saved1.getPublicId()).isNotNull();
        assertThat(saved2.getPublicId()).isNotNull();
        assertThat(saved1.getPublicId()).isNotEqualTo(saved2.getPublicId());
    }

    @Test
    @DisplayName("Deve buscar usuário por ID")
    void shouldFindUsuarioById() {
        // When
        Optional<Usuario> found = usuarioRepository.findById(usuario1.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("neto");
        assertThat(found.get().getPassword()).isEqualTo("senha123");
        assertThat(found.get().getRole()).isEqualTo("ROLE_ESTAGIARIO");
    }

    @Test
    @DisplayName("Deve retornar vazio quando ID não existe")
    void shouldReturnEmptyWhenIdNotFound() {
        // Given
        Long idInexistente = 99999L;

        // When
        Optional<Usuario> found = usuarioRepository.findById(idInexistente);

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
        assertThat(usuarios)
                .extracting(Usuario::getUsername)
                .containsExactlyInAnyOrder("neto", "admin");
    }

    @Test
    @DisplayName("Deve buscar todos os usuários paginados")
    void shouldFindAllUsuariosPaginated() {
        // Given
        Usuario usuario3 = Usuario.builder()
                .username("user3")
                .password("pass3")
                .role("ROLE_ESTAGIARIO")
                .build();
        usuarioRepository.save(usuario3);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Usuario> page = usuarioRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    @DisplayName("Deve buscar usuários com ordenação")
    void shouldFindUsuariosWithSorting() {
        // Given
        Sort sort = Sort.by(Sort.Direction.ASC, "username");

        // When
        List<Usuario> usuarios = usuarioRepository.findAll(sort);

        // Then
        assertThat(usuarios).hasSize(2);
        assertThat(usuarios.get(0).getUsername()).isEqualTo("admin");
        assertThat(usuarios.get(1).getUsername()).isEqualTo("neto");
    }

    @Test
    @DisplayName("Deve atualizar usuário existente")
    void shouldUpdateExistingUsuario() {
        // Given
        Usuario usuarioExistente = usuarioRepository.findByUsername("neto").orElseThrow();
        Long idOriginal = usuarioExistente.getId();
        UUID publicIdOriginal = usuarioExistente.getPublicId();

        usuarioExistente.setPassword("novaSenha456");
        usuarioExistente.setRole("ROLE_ADMIN");

        // When
        usuarioRepository.save(usuarioExistente);
        entityManager.flush();
        entityManager.clear();

        // Then
        Usuario found = usuarioRepository.findById(idOriginal).orElseThrow();
        assertThat(found.getId()).isEqualTo(idOriginal);
        assertThat(found.getPublicId()).isEqualTo(publicIdOriginal); // Não deve mudar
        assertThat(found.getUsername()).isEqualTo("neto");
        assertThat(found.getPassword()).isEqualTo("novaSenha456");
        assertThat(found.getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Deve atualizar apenas a senha do usuário")
    void shouldUpdateOnlyPassword() {
        // Given
        Usuario usuario = usuarioRepository.findByUsername("neto").orElseThrow();
        String senhaOriginal = usuario.getPassword();
        String roleOriginal = usuario.getRole();

        // When
        usuario.setPassword("novaSenha999");
        Usuario updated = usuarioRepository.save(usuario);
        entityManager.flush();

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
        Usuario usuario = usuarioRepository.findByUsername("neto").orElseThrow();
        String senhaOriginal = usuario.getPassword();

        // When
        usuario.setRole("ROLE_MANAGER");
        Usuario updated = usuarioRepository.save(usuario);
        entityManager.flush();

        // Then
        assertThat(updated.getRole()).isEqualTo("ROLE_MANAGER");
        assertThat(updated.getPassword()).isEqualTo(senhaOriginal);
        assertThat(updated.getUsername()).isEqualTo("neto");
    }

    @Test
    @DisplayName("Deve atualizar username e validar persistência")
    void shouldUpdateUsernameAndValidatePersistence() {
        // Given
        Usuario usuario = usuarioRepository.findByUsername("neto").orElseThrow();
        Long userId = usuario.getId();

        // When
        usuario.setUsername("neto_updated");
        usuarioRepository.save(usuario);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Usuario> found = usuarioRepository.findById(userId);
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("neto_updated");
        assertThat(usuarioRepository.findByUsername("neto")).isEmpty();
        assertThat(usuarioRepository.findByUsername("neto_updated")).isPresent();
    }

    @Test
    @DisplayName("Deve deletar usuário por ID")
    void shouldDeleteUsuarioById() {
        // Given
        Long idParaDeletar = usuario1.getId();

        // When
        usuarioRepository.deleteById(idParaDeletar);
        entityManager.flush();

        // Then
        Optional<Usuario> deleted = usuarioRepository.findById(idParaDeletar);
        assertThat(deleted).isEmpty();

        List<Usuario> remaining = usuarioRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.getFirst().getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Deve deletar usuário por entidade")
    void shouldDeleteUsuarioByEntity() {
        // When
        usuarioRepository.delete(usuario2);
        entityManager.flush();

        // Then
        Optional<Usuario> deleted = usuarioRepository.findByUsername("admin");
        assertThat(deleted).isEmpty();

        List<Usuario> remaining = usuarioRepository.findAll();
        assertThat(remaining).hasSize(1);
        assertThat(remaining.getFirst().getUsername()).isEqualTo("neto");
    }

    @Test
    @DisplayName("Deve deletar usuário e validar que não existe mais")
    void shouldDeleteAndValidateNonExistence() {
        // Given
        Long userId = usuario1.getId();
        String username = usuario1.getUsername();

        // When
        usuarioRepository.deleteById(userId);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(usuarioRepository.existsById(userId)).isFalse();
        assertThat(usuarioRepository.findById(userId)).isEmpty();
        assertThat(usuarioRepository.findByUsername(username)).isEmpty();
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
    @DisplayName("Deve deletar todos os usuários")
    void shouldDeleteAllUsuarios() {
        // When
        usuarioRepository.deleteAll();
        entityManager.flush();

        // Then
        assertThat(usuarioRepository.count()).isZero();
        assertThat(usuarioRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar que deleteAll remove todos os usuários")
    void shouldVerifyDeleteAllRemovesAllUsuarios() {
        // Given
        long countBefore = usuarioRepository.count();
        assertThat(countBefore).isEqualTo(2);

        // When
        usuarioRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(usuarioRepository.count()).isZero();
        assertThat(usuarioRepository.findAll()).isEmpty();
        assertThat(usuarioRepository.findByUsername("neto")).isEmpty();
        assertThat(usuarioRepository.findByUsername("admin")).isEmpty();
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
        boolean notExists = usuarioRepository.existsById(99999L);

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    // ==================== TESTES findByUsername ====================

    @Test
    @DisplayName("Deve buscar usuário por username exato")
    void shouldFindByUsernameExact() {
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
    @DisplayName("Deve retornar vazio ao buscar com null")
    void shouldReturnEmptyWhenSearchingWithNull() {
        // When
        Optional<Usuario> found = usuarioRepository.findByUsername(null);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar com string vazia")
    void shouldReturnEmptyWhenSearchingWithEmptyString() {
        // When
        Optional<Usuario> found = usuarioRepository.findByUsername("");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar username com espaços em branco")
    void shouldFindByUsernameWithWhitespace() {
        // Given
        Usuario usuario = Usuario.builder()
                .username("user with spaces")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();
        usuarioRepository.save(usuario);
        entityManager.flush();

        // When
        Optional<Usuario> found = usuarioRepository.findByUsername("user with spaces");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("user with spaces");
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

    // ==================== TESTES findByUsernameIgnoreCase (MÉTODO CUSTOMIZADO COM @Query) ====================

    @Test
    @DisplayName("Deve buscar usuário ignorando case - lowercase")
    void shouldFindByUsernameIgnoreCaseLowercase() {
        // When
        Optional<Usuario> found = usuarioRepository.findByUsernameIgnoreCase("neto");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("neto");
    }

    @Test
    @DisplayName("Deve buscar usuário ignorando case - UPPERCASE")
    void shouldFindByUsernameIgnoreCaseUppercase() {
        // When
        Optional<Usuario> found = usuarioRepository.findByUsernameIgnoreCase("NETO");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("neto");
        assertThat(found.get().getRole()).isEqualTo("ROLE_ESTAGIARIO");
    }

    @Test
    @DisplayName("Deve buscar usuário ignorando case - MixedCase")
    void shouldFindByUsernameIgnoreCaseMixedCase() {
        // When
        Optional<Usuario> found = usuarioRepository.findByUsernameIgnoreCase("NeTo");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("neto");
    }

    @Test
    @DisplayName("Deve buscar admin ignorando case")
    void shouldFindAdminIgnoringCase() {
        // When
        Optional<Usuario> foundLower = usuarioRepository.findByUsernameIgnoreCase("admin");
        Optional<Usuario> foundUpper = usuarioRepository.findByUsernameIgnoreCase("ADMIN");
        Optional<Usuario> foundMixed = usuarioRepository.findByUsernameIgnoreCase("AdMiN");

        // Then
        assertThat(foundLower).isPresent();
        assertThat(foundUpper).isPresent();
        assertThat(foundMixed).isPresent();

        assertThat(foundLower.get().getUsername()).isEqualTo("admin");
        assertThat(foundUpper.get().getUsername()).isEqualTo("admin");
        assertThat(foundMixed.get().getUsername()).isEqualTo("admin");

        assertThat(foundLower.get().getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Deve retornar vazio ao buscar inexistente com ignore case")
    void shouldReturnEmptyWhenUsernameNotFoundIgnoreCase() {
        // When
        Optional<Usuario> found = usuarioRepository.findByUsernameIgnoreCase("INEXISTENTE");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve lidar com null no ignore case")
    void shouldHandleNullInIgnoreCase() {
        // When
        Optional<Usuario> found = usuarioRepository.findByUsernameIgnoreCase(null);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve lidar com string vazia no ignore case")
    void shouldHandleEmptyStringInIgnoreCase() {
        // When
        Optional<Usuario> found = usuarioRepository.findByUsernameIgnoreCase("");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar com espaços extras ignorando case")
    void shouldFindWithExtraSpacesIgnoreCase() {
        // Given
        Usuario usuario = Usuario.builder()
                .username("user test")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();
        usuarioRepository.save(usuario);
        entityManager.flush();

        // When
        Optional<Usuario> found = usuarioRepository.findByUsernameIgnoreCase("USER TEST");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("user test");
    }

    @Test
    @DisplayName("Deve buscar usuário com caracteres especiais ignorando case")
    void shouldFindUserWithSpecialCharsIgnoringCase() {
        // Given
        Usuario usuario = Usuario.builder()
                .username("user_name-123")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();
        usuarioRepository.save(usuario);
        entityManager.flush();

        // When
        Optional<Usuario> found = usuarioRepository.findByUsernameIgnoreCase("USER_NAME-123");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("user_name-123");
    }

    // ==================== TESTES DE VALIDAÇÃO ====================

    @Test
    @DisplayName("Não deve permitir username duplicado")
    void shouldNotAllowDuplicateUsername() {
        // Given
        Usuario usuarioDuplicado = Usuario.builder()
                .username("neto")
                .password("outraSenha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When & Then
        assertThatThrownBy(() -> {
            usuarioRepository.save(usuarioDuplicado);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
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
        entityManager.flush();

        // Then
        List<Usuario> usuarios = usuarioRepository.findAll();
        assertThat(usuarios).hasSize(4);
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
        }).isInstanceOf(Exception.class);
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
    @DisplayName("Não deve permitir username vazio")
    void shouldNotAllowEmptyUsername() {
        // Given
        Usuario usuarioComUsernameVazio = Usuario.builder()
                .username("")
                .password("senha123")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When & Then
        assertThatThrownBy(() -> {
            usuarioRepository.save(usuarioComUsernameVazio);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Username não pode ser vazio");
    }

    @Test
    @DisplayName("Não deve permitir password vazio")
    void shouldNotAllowEmptyPassword() {
        // Given
        Usuario usuarioComPasswordVazio = Usuario.builder()
                .username("emptypass")
                .password("")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When & Then
        assertThatThrownBy(() -> {
            usuarioRepository.save(usuarioComPasswordVazio);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Password não pode ser vazio");
    }

    @Test
    @DisplayName("Não deve permitir role vazia")
    void shouldNotAllowEmptyRole() {
        // Given
        Usuario usuarioComRoleVazia = Usuario.builder()
                .username("user123")
                .password("senha123")
                .role("")
                .build();

        // When & Then
        assertThatThrownBy(() -> {
            usuarioRepository.save(usuarioComRoleVazia);
            entityManager.flush();
        }).isInstanceOf(ConstraintViolationException.class)
                .hasMessageContaining("Role não pode ser vazia");
    }

    @Test
    @DisplayName("Deve validar tamanho máximo do username")
    void shouldValidateUsernameMaxLength() {
        // Given
        String usernameLongo = "a".repeat(256);
        Usuario usuario = Usuario.builder()
                .username(usernameLongo)
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When & Then
        assertThatThrownBy(() -> {
            usuarioRepository.save(usuario);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Deve validar formato de roles válidas")
    void shouldValidateValidRoleFormats() {
        // Given
        Usuario estagiario = Usuario.builder()
                .username("estagiario1")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        Usuario admin = Usuario.builder()
                .username("admin1")
                .password("senha")
                .role("ROLE_ADMIN")
                .build();

        // When
        Usuario savedEstagiario = usuarioRepository.save(estagiario);
        Usuario savedAdmin = usuarioRepository.save(admin);
        entityManager.flush();

        // Then
        assertThat(savedEstagiario.getRole()).isEqualTo("ROLE_ESTAGIARIO");
        assertThat(savedAdmin.getRole()).isEqualTo("ROLE_ADMIN");
    }

    // ==================== TESTES DE NORMALIZAÇÃO DE USERNAME ====================

    @Test
    @DisplayName("Username deve ser convertido para lowercase automaticamente")
    void shouldConvertUsernameToLowercaseAutomatically() {
        // Given
        Usuario usuario = Usuario.builder()
                .username("MaRiA")
                .password("senha123")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When
        Usuario saved = usuarioRepository.save(usuario);
        entityManager.flush();
        entityManager.clear();

        // Then
        Usuario found = usuarioRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getUsername()).isEqualTo("maria");
    }

    @Test
    @DisplayName("Username com UPPERCASE deve ser normalizado")
    void shouldNormalizeUppercaseUsername() {
        // Given
        Usuario usuario = Usuario.builder()
                .username("CARLOS")
                .password("senha")
                .role("ROLE_ADMIN")
                .build();

        // When
        Usuario saved = usuarioRepository.save(usuario);
        entityManager.flush();

        // Then
        assertThat(saved.getUsername()).isEqualTo("carlos");
    }

    @Test
    @DisplayName("Username com espaços deve ser trimado e normalizado")
    void shouldTrimAndNormalizeUsernameWithSpaces() {
        // Given
        Usuario usuario = Usuario.builder()
                .username("  PeDrO  ")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When
        Usuario saved = usuarioRepository.save(usuario);
        entityManager.flush();

        // Then
        assertThat(saved.getUsername()).isEqualTo("pedro");
    }

    @Test
    @DisplayName("Deve encontrar usuário normalizado por findByUsernameIgnoreCase")
    void shouldFindNormalizedUsernameByIgnoreCase() {
        // Given
        Usuario usuario = Usuario.builder()
                .username("JoAo")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();
        usuarioRepository.save(usuario);
        entityManager.flush();

        // When
        Optional<Usuario> found1 = usuarioRepository.findByUsernameIgnoreCase("joao");
        Optional<Usuario> found2 = usuarioRepository.findByUsernameIgnoreCase("JOAO");
        Optional<Usuario> found3 = usuarioRepository.findByUsernameIgnoreCase("JoAo");

        // Then
        assertThat(found1).isPresent();
        assertThat(found2).isPresent();
        assertThat(found3).isPresent();
        assertThat(found1.get().getUsername()).isEqualTo("joao");
    }

    // ==================== TESTES DE RELACIONAMENTO ====================

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
        entityManager.flush();

        // Then
        assertThat(saved.getRegistros()).isNotNull();
        assertThat(saved.getRegistros()).isEmpty();
    }

    @Test
    @DisplayName("Deve validar que registros inicializam como lista vazia")
    void shouldValidateRegistrosInitializeAsEmptyList() {
        // Given
        Usuario usuario = Usuario.builder()
                .username("newuser")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When
        Usuario saved = usuarioRepository.save(usuario);
        entityManager.flush();
        entityManager.clear();

        // Then
        Usuario found = usuarioRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getRegistros()).isNotNull();
        assertThat(found.getRegistros()).isEmpty();
    }

    @Test
    @DisplayName("Deve manter relacionamento ao adicionar registros de horas")
    void shouldMaintainRelationshipWhenAddingRegisterHoras() {
        // Given
        Usuario usuario = usuarioRepository.findByUsername("neto").orElseThrow();

        RegisterHoras registro = RegisterHoras.builder()
                .estagiario("neto")
                .descricao("Teste")
                .dataInicio(LocalDateTime.now())
                .dataFim(LocalDateTime.now().plusHours(8))
                .horasTrabalhadas(8)
                .usuario(usuario)
                .build();

        if (usuario.getRegistros() == null) {
            usuario.setRegistros(new ArrayList<>());
        }
        usuario.getRegistros().add(registro);

        // When
        Usuario saved = usuarioRepository.save(usuario);
        entityManager.flush();
        entityManager.clear();

        // Then
        Usuario found = usuarioRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getRegistros()).isNotEmpty();
        assertThat(found.getRegistros()).hasSize(1);
        assertThat(found.getRegistros().getFirst().getDescricao()).isEqualTo("Teste");
    }

    @Test
    @DisplayName("Deve deletar registros em cascata ao deletar usuário")
    void shouldDeleteRegistrosInCascadeWhenDeletingUsuario() {
        // Given
        Usuario usuario = usuarioRepository.findByUsername("neto").orElseThrow();

        RegisterHoras registro = RegisterHoras.builder()
                .estagiario("neto")
                .descricao("Teste Cascata")
                .dataInicio(LocalDateTime.now())
                .dataFim(LocalDateTime.now().plusHours(8))
                .horasTrabalhadas(8)
                .usuario(usuario)
                .build();

        if (usuario.getRegistros() == null) {
            usuario.setRegistros(new ArrayList<>());
        }
        usuario.getRegistros().add(registro);
        usuarioRepository.save(usuario);
        entityManager.flush();

        Long usuarioId = usuario.getId();

        // When
        usuarioRepository.deleteById(usuarioId);
        entityManager.flush();

        // Then
        assertThat(usuarioRepository.findById(usuarioId)).isEmpty();
    }

    @Test
    @DisplayName("Deve remover órfãos ao limpar lista de registros")
    void shouldRemoveOrphansWhenClearingRegistros() {
        // Given
        Usuario usuario = usuarioRepository.findByUsername("neto").orElseThrow();

        RegisterHoras registro = RegisterHoras.builder()
                .estagiario("neto")
                .descricao("Teste")
                .dataInicio(LocalDateTime.now())
                .dataFim(LocalDateTime.now().plusHours(8))
                .horasTrabalhadas(8)
                .usuario(usuario)
                .build();

        usuario.getRegistros().add(registro);
        usuarioRepository.save(usuario);
        entityManager.flush();

        // When
        usuario.getRegistros().clear();
        usuarioRepository.save(usuario);
        entityManager.flush();
        entityManager.clear();

        // Then
        Usuario found = usuarioRepository.findById(usuario.getId()).orElseThrow();
        assertThat(found.getRegistros()).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar usuário e verificar lazy loading de registros")
    void shouldFindUsuarioAndVerifyLazyLoadingOfRegistros() {
        // When
        Usuario found = usuarioRepository.findByUsername("neto").orElseThrow();

        // Then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isNotNull();
        assertThat(found.getRegistros()).isNotNull();
    }

    // ==================== TESTES DE EDGE CASES ====================

    @Test
    @DisplayName("Deve permitir caracteres especiais no username")
    void shouldAllowSpecialCharactersInUsername() {
        // Given
        Usuario usuario = Usuario.builder()
                .username("user.name_123-test")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When
        Usuario saved = usuarioRepository.save(usuario);
        entityManager.flush();
        Optional<Usuario> found = usuarioRepository.findByUsername("user.name_123-test");

        // Then
        assertThat(saved.getUsername()).isEqualTo("user.name_123-test");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("user.name_123-test");
    }

    @Test
    @DisplayName("Deve permitir caracteres unicode no username")
    void shouldAllowUnicodeCharactersInUsername() {
        // Given
        Usuario usuario = Usuario.builder()
                .username("usuário_日本語")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When
        usuarioRepository.save(usuario);
        entityManager.flush();
        Optional<Usuario> found = usuarioRepository.findByUsername("usuário_日本語");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("usuário_日本語");
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

    @Test
    @DisplayName("Deve validar comportamento de saveAndFlush")
    void shouldValidateSaveAndFlushBehavior() {
        // Given
        Usuario usuario = Usuario.builder()
                .username("flushuser")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When
        Usuario saved = usuarioRepository.saveAndFlush(usuario);
        entityManager.clear();

        // Then
        Optional<Usuario> found = usuarioRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("flushuser");
    }

    @Test
    @DisplayName("Deve salvar múltiplos usuários em lote")
    void shouldSaveMultipleUsuariosInBatch() {
        // Given
        List<Usuario> novosUsuarios = List.of(
                Usuario.builder().username("user1").password("pass1").role("ROLE_ESTAGIARIO").build(),
                Usuario.builder().username("user2").password("pass2").role("ROLE_ADMIN").build(),
                Usuario.builder().username("user3").password("pass3").role("ROLE_ESTAGIARIO").build()
        );

        // When
        usuarioRepository.saveAll(novosUsuarios);
        entityManager.flush();
        List<Usuario> todos = usuarioRepository.findAll();

        // Then
        assertThat(todos).hasSize(5); // 2 do setUp + 3 novos
        assertThat(todos)
                .extracting(Usuario::getUsername)
                .contains("user1", "user2", "user3", "neto", "admin");
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
        assertThat(neto.getPublicId()).isNotNull();
        assertThat(neto.getPassword()).isEqualTo("senha123");
        assertThat(neto.getRole()).isEqualTo("ROLE_ESTAGIARIO");

        assertThat(admin.getId()).isNotNull();
        assertThat(admin.getPublicId()).isNotNull();
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
        entityManager.flush();

        // Then
        assertThat(saved1.getId()).isNotNull();
        assertThat(saved2.getId()).isNotNull();
        assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
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
    @DisplayName("Deve verificar comportamento do flush após save")
    void shouldVerifyFlushBehaviorAfterSave() {
        // Given
        Usuario usuario = Usuario.builder()
                .username("flushtest")
                .password("senha")
                .role("ROLE_ESTAGIARIO")
                .build();

        // When
        Usuario saved = usuarioRepository.save(usuario);
        // ID já foi gerado
        assertThat(saved.getId()).isNotNull();

        // Força flush
        entityManager.flush();
        entityManager.clear();

        // Busca do banco
        Optional<Usuario> found = usuarioRepository.findByUsername("flushtest");
        assertThat(found).isPresent();
    }
}