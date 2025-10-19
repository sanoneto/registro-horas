package com.registo.horas_estagio.repository;

import com.registo.horas_estagio.models.RegisterHoras;
import com.registo.horas_estagio.models.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes do RegistroHorasRepository")
class RegistroHorasRepositoryTest {

    @Autowired
    private RegistroHorasRepository registroHorasRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Usuario usuario1;
    private Usuario usuario2;
    private RegisterHoras registro1;
    private RegisterHoras registro2;
    private RegisterHoras registro3;

    @BeforeEach
    void setUp() {
        // Limpar dados existentes
        registroHorasRepository.deleteAll();
        usuarioRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Criar usuários
        usuario1 = Usuario.builder()
                .username("neto")
                .password("senha123")
                .role("ROLE_ESTAGIARIO")
                .build();

        usuario2 = Usuario.builder()
                .username("joao")
                .password("senha456")
                .role("ROLE_ESTAGIARIO")
                .build();

        usuarioRepository.saveAll(List.of(usuario1, usuario2));

        // Criar registros de horas
        registro1 = RegisterHoras.builder()
                .estagiario("neto")
                .descricao("Desenvolvimento de API REST")
                .dataInicio(LocalDateTime.of(2024, 1, 15, 9, 0))
                .dataFim(LocalDateTime.of(2024, 1, 15, 18, 0))
                .horasTrabalhadas(9)
                .usuario(usuario1)
                .build();

        registro2 = RegisterHoras.builder()
                .estagiario("neto")
                .descricao("Testes unitários")
                .dataInicio(LocalDateTime.of(2024, 1, 16, 9, 0))
                .dataFim(LocalDateTime.of(2024, 1, 16, 17, 0))
                .horasTrabalhadas(8)
                .usuario(usuario1)
                .build();

        registro3 = RegisterHoras.builder()
                .estagiario("joao")
                .descricao("Documentação")
                .dataInicio(LocalDateTime.of(2024, 1, 15, 10, 0))
                .dataFim(LocalDateTime.of(2024, 1, 15, 16, 0))
                .horasTrabalhadas(6)
                .usuario(usuario2)
                .build();

        registroHorasRepository.saveAll(List.of(registro1, registro2, registro3));
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Deve salvar registro de horas com sucesso")
    void shouldSaveRegisterHoras() {
        // Given
        RegisterHoras novoRegistro = RegisterHoras.builder()
                .estagiario("neto")
                .descricao("Code Review")
                .dataInicio(LocalDateTime.of(2024, 1, 17, 14, 0))
                .dataFim(LocalDateTime.of(2024, 1, 17, 16, 0))
                .horasTrabalhadas(2)
                .usuario(usuario1)
                .build();

        // When
        RegisterHoras saved = registroHorasRepository.save(novoRegistro);

        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEstagiario()).isEqualTo("neto");
        assertThat(saved.getDescricao()).isEqualTo("Code Review");
        assertThat(saved.getHorasTrabalhadas()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deve buscar registro por ID")
    void shouldFindRegisterById() {
        // When
        Optional<RegisterHoras> found = registroHorasRepository.findById(registro1.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEstagiario()).isEqualTo("neto");
        assertThat(found.get().getDescricao()).isEqualTo("Desenvolvimento de API REST");
    }

    @Test
    @DisplayName("Deve retornar vazio quando ID não existe")
    void shouldReturnEmptyWhenIdNotFound() {
        // Given
        UUID idInexistente = UUID.randomUUID();

        // When
        Optional<RegisterHoras> found = registroHorasRepository.findById(idInexistente);

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar registros por estagiário")
    void shouldFindByEstagiario() {
        // When
        List<RegisterHoras> registrosNeto = registroHorasRepository.findByEstagiario("neto");

        // Then
        assertThat(registrosNeto).isNotEmpty();
        assertThat(registrosNeto).hasSize(2);
        assertThat(registrosNeto)
                .extracting(RegisterHoras::getEstagiario)
                .containsOnly("neto");
        assertThat(registrosNeto)
                .extracting(RegisterHoras::getDescricao)
                .containsExactlyInAnyOrder("Desenvolvimento de API REST", "Testes unitários");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando estagiário não tem registros")
    void shouldReturnEmptyListWhenEstagiarioHasNoRegisters() {
        // When
        List<RegisterHoras> registros = registroHorasRepository.findByEstagiario("inexistente");

        // Then
        assertThat(registros).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar registros paginados por estagiário")
    void shouldFindByEstagiarioPaginated() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RegisterHoras> page = registroHorasRepository.findByEstagiario("neto", pageable);

        // Then
        assertThat(page).isNotNull();
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(1);
        assertThat(page.getContent())
                .extracting(RegisterHoras::getEstagiario)
                .containsOnly("neto");
    }

    @Test
    @DisplayName("Deve paginar corretamente com tamanho de página menor")
    void shouldPaginateCorrectlyWithSmallerPageSize() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<RegisterHoras> page = registroHorasRepository.findByEstagiario("neto", pageable);

        // Then
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.isFirst()).isTrue();
    }

    @Test
    @DisplayName("Deve buscar todos os registros")
    void shouldFindAllRegisters() {
        // When
        List<RegisterHoras> registros = registroHorasRepository.findAll();

        // Then
        assertThat(registros).isNotEmpty();
        assertThat(registros).hasSize(3);
        assertThat(registros)
                .extracting(RegisterHoras::getEstagiario)
                .containsExactlyInAnyOrder("neto", "neto", "joao");
    }

    @Test
    @DisplayName("Deve buscar todos os registros paginados")
    void shouldFindAllRegistersPaginated() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<RegisterHoras> page = registroHorasRepository.findAll(pageable);

        // Then
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.hasNext()).isTrue();
    }

    @Test
    @DisplayName("Deve atualizar registro existente")
    void shouldUpdateExistingRegister() {
        // Given
        RegisterHoras registroExistente = registroHorasRepository
                .findById(registro1.getId()).get();
        registroExistente.setDescricao("Desenvolvimento de API REST - Atualizado");
        registroExistente.setHorasTrabalhadas(10);

        // When
        RegisterHoras updated = registroHorasRepository.save(registroExistente);

        // Then
        assertThat(updated.getId()).isEqualTo(registro1.getId());
        assertThat(updated.getDescricao()).isEqualTo("Desenvolvimento de API REST - Atualizado");
        assertThat(updated.getHorasTrabalhadas()).isEqualTo(10);
        assertThat(updated.getEstagiario()).isEqualTo("neto");
    }

    @Test
    @DisplayName("Deve deletar registro por ID")
    void shouldDeleteRegisterById() {
        // Given
        UUID idParaDeletar = registro1.getId();

        // When
        registroHorasRepository.deleteById(idParaDeletar);

        // Then
        Optional<RegisterHoras> deleted = registroHorasRepository.findById(idParaDeletar);
        assertThat(deleted).isEmpty();

        List<RegisterHoras> remaining = registroHorasRepository.findAll();
        assertThat(remaining).hasSize(2);
    }

    @Test
    @DisplayName("Deve deletar registro por entidade")
    void shouldDeleteRegisterByEntity() {
        // When
        registroHorasRepository.delete(registro2);

        // Then
        Optional<RegisterHoras> deleted = registroHorasRepository.findById(registro2.getId());
        assertThat(deleted).isEmpty();

        List<RegisterHoras> remaining = registroHorasRepository.findAll();
        assertThat(remaining).hasSize(2);
    }

    @Test
    @DisplayName("Deve deletar todos os registros de um estagiário")
    void shouldDeleteAllRegistersByEstagiario() {
        // Given
        List<RegisterHoras> registrosNeto = registroHorasRepository.findByEstagiario("neto");

        // When
        registroHorasRepository.deleteAll(registrosNeto);

        // Then
        List<RegisterHoras> remaining = registroHorasRepository.findByEstagiario("neto");
        assertThat(remaining).isEmpty();

        List<RegisterHoras> all = registroHorasRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getEstagiario()).isEqualTo("joao");
    }

    @Test
    @DisplayName("Deve contar total de registros")
    void shouldCountTotalRegisters() {
        // When
        long count = registroHorasRepository.count();

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve verificar se registro existe por ID")
    void shouldCheckIfRegisterExistsById() {
        // When
        boolean exists = registroHorasRepository.existsById(registro1.getId());
        boolean notExists = registroHorasRepository.existsById(UUID.randomUUID());

        // Then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Deve manter relacionamento com usuário")
    void shouldMaintainUserRelationship() {
        // When
        Optional<RegisterHoras> found = registroHorasRepository.findById(registro1.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsuario()).isNotNull();
        assertThat(found.get().getUsuario().getUsername()).isEqualTo("neto");
        assertThat(found.get().getUsuario().getId()).isEqualTo(usuario1.getId());
    }

    @Test
    @DisplayName("Deve persistir datas corretamente")
    void shouldPersistDatesCorrectly() {
        // Given
        LocalDateTime inicio = LocalDateTime.of(2024, 6, 15, 9, 30);
        LocalDateTime fim = LocalDateTime.of(2024, 6, 15, 18, 30);

        RegisterHoras registro = RegisterHoras.builder()
                .estagiario("neto")
                .descricao("Desenvolvimento")
                .dataInicio(inicio)
                .dataFim(fim)
                .horasTrabalhadas(9)
                .usuario(usuario1)
                .build();

        // When
        RegisterHoras saved = registroHorasRepository.save(registro);
        entityManager.flush();
        entityManager.clear();

        Optional<RegisterHoras> found = registroHorasRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getDataInicio()).isEqualTo(inicio);
        assertThat(found.get().getDataFim()).isEqualTo(fim);
    }

    @Test
    @DisplayName("Deve ordenar registros por data de início")
    void shouldOrderRegistersByStartDate() {
        // Given

        // When
        List<RegisterHoras> registros = registroHorasRepository.findAll();

        // Then
        assertThat(registros).isNotEmpty();
        assertThat(registros.get(0).getDataInicio()).isNotNull();
    }

    @Test
    @DisplayName("Deve permitir múltiplos registros para o mesmo usuário")
    void shouldAllowMultipleRegistersForSameUser() {
        // When
        List<RegisterHoras> registrosNeto = registroHorasRepository.findByEstagiario("neto");

        // Then
        assertThat(registrosNeto).hasSize(2);
        assertThat(registrosNeto)
                .allMatch(r -> r.getUsuario().getUsername().equals("neto"));
    }

    @Test
    @DisplayName("Deve limpar todos os registros")
    void shouldDeleteAllRegisters() {
        // When
        registroHorasRepository.deleteAll();

        // Then
        List<RegisterHoras> all = registroHorasRepository.findAll();
        assertThat(all).isEmpty();
        assertThat(registroHorasRepository.count()).isZero();
    }
}