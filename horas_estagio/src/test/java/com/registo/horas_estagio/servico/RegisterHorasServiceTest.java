package com.registo.horas_estagio.servico;

import com.registo.horas_estagio.dto.request.RegisterRequest;
import com.registo.horas_estagio.dto.response.PageResponse;
import com.registo.horas_estagio.dto.response.RegisterResponse;
import com.registo.horas_estagio.mapper.RequestMapper;
import com.registo.horas_estagio.models.RegisterHoras;
import com.registo.horas_estagio.models.Usuario;
import com.registo.horas_estagio.repository.RegistroHorasRepository;
import com.registo.horas_estagio.repository.UsuarioRepository;
import com.registo.horas_estagio.service.impl.RegisterHorasServiceImpl;
import com.registo.horas_estagio.util.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do RegisterHorasService")
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterHorasServiceTest {

    @Mock
    private RegistroHorasRepository registroHorasRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RequestMapper requestMapper;

    @InjectMocks
    private RegisterHorasServiceImpl registerHorasService;

    private RegisterRequest registerRequest;
    private RegisterHoras registerHoras;
    private RegisterResponse registerResponse;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        registerRequest = ObjectUtils.createDefaultRequest();
        registerHoras = ObjectUtils.createDefaultRegisterHoras();
        registerResponse = ObjectUtils.createDefaultResponse();
        usuario = ObjectUtils.createDefaultUsuario();
    }

    @Test
    @DisplayName("Deve criar registro de horas com sucesso")
    void shouldCreateRegisterHorasSuccessfully() {
        // Given
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));
        when(requestMapper.mapToRegisterHoras(registerRequest)).thenReturn(registerHoras);
        when(registroHorasRepository.save(any(RegisterHoras.class))).thenReturn(registerHoras);
        when(requestMapper.mapRegisterResponse(registerHoras)).thenReturn(registerResponse);

        // When
        RegisterResponse result = registerHorasService.submitHours(registerRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.estagiario()).isEqualTo("neto");
        assertThat(result.descricao()).isEqualTo("Desenvolvimento de API REST");
        assertThat(result.horasTrabalhadas()).isEqualTo(9);

        verify(usuarioRepository).findByUsername("neto");
        verify(registroHorasRepository).save(any(RegisterHoras.class));
        verify(requestMapper).mapRegisterResponse(registerHoras);
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não existe")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.empty());
        when(requestMapper.mapToRegisterHoras(registerRequest)).thenReturn(registerHoras);

        // When & Then
        assertThatThrownBy(() -> registerHorasService.submitHours(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado");

        verify(usuarioRepository).findByUsername("neto");
        verify(registroHorasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve calcular horas automaticamente quando não fornecidas")
    void shouldCalculateHoursAutomatically() {
        // Given
        RegisterRequest requestSemHoras = new RegisterRequest(
                "neto",
                "Desenvolvimento",
                LocalDateTime.of(2024, 1, 15, 9, 0),
                LocalDateTime.of(2024, 1, 15, 13, 0),
                0  // Horas não fornecidas
        );

        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));
        when(requestMapper.mapToRegisterHoras(requestSemHoras)).thenReturn(registerHoras);
        when(registroHorasRepository.save(any(RegisterHoras.class))).thenReturn(registerHoras);
        when(requestMapper.mapRegisterResponse(any())).thenReturn(registerResponse);

        // When
        registerHorasService.submitHours(requestSemHoras);

        // Then
        verify(registroHorasRepository).save(any(RegisterHoras.class));
    }

    @Test
    @DisplayName("Deve buscar todos os registros")
    void shouldFindAllRegisteredHours() {
        // Given
        List<RegisterHoras> registros = List.of(registerHoras);
        when(registroHorasRepository.findAll()).thenReturn(registros);
        when(requestMapper.mapToListRegisterResponse(registros))
                .thenReturn(List.of(registerResponse));

        // When
        List<RegisterResponse> result = registerHorasService.findAllRegisteredHours();

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().estagiario()).isEqualTo("neto");

        verify(registroHorasRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar registros paginados")
    void shouldFindAllRegisteredHoursPaginated() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<RegisterHoras> page = new PageImpl<>(List.of(registerHoras), pageable, 1);

        when(registroHorasRepository.findAll(pageable)).thenReturn(page);
        when(requestMapper.mapToListRegisterResponse(anyList()))
                .thenReturn(List.of(registerResponse));

        // When
        PageResponse<RegisterResponse> result =
                registerHorasService.findAllRegisteredHours(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.first()).isTrue();
        assertThat(result.last()).isTrue();

        verify(registroHorasRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve buscar registros por usuário")
    void shouldFindRegisteredHoursByUser() {
        // Given
        List<RegisterHoras> registros = List.of(registerHoras);
        when(registroHorasRepository.findByEstagiario("neto")).thenReturn(registros);
        when(requestMapper.mapToListRegisterResponse(registros))
                .thenReturn(List.of(registerResponse));

        // When
        List<RegisterResponse> result =
                registerHorasService.findAllRegisteredHoursUser("neto");

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().estagiario()).isEqualTo("neto");

        verify(registroHorasRepository).findByEstagiario("neto");
    }

    @Test
    @DisplayName("Deve atualizar registro com sucesso")
    void shouldUpdateRegisterSuccessfully() {
        // Given
        UUID uuid = UUID.randomUUID();
        when(registroHorasRepository.findById(uuid)).thenReturn(Optional.of(registerHoras));
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));
        when(registroHorasRepository.save(any(RegisterHoras.class))).thenReturn(registerHoras);
        when(requestMapper.mapRegisterResponse(registerHoras)).thenReturn(registerResponse);

        // When
        RegisterResponse result = registerHorasService.updateRegister(uuid, registerRequest);

        // Then
        assertThat(result).isNotNull();
        verify(registroHorasRepository).findById(uuid);
        verify(registroHorasRepository).save(any(RegisterHoras.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar registro inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentRegister() {
        // Given
        UUID uuid = UUID.randomUUID();
        when(registroHorasRepository.findById(uuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> registerHorasService.updateRegister(uuid, registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Registro não encontrado");

        verify(registroHorasRepository).findById(uuid);
        verify(registroHorasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar registro com sucesso")
    void shouldDeleteRegisterSuccessfully() {
        // Given
        UUID uuid = UUID.randomUUID();
        when(registroHorasRepository.findById(uuid)).thenReturn(Optional.of(registerHoras));
        doNothing().when(registroHorasRepository).delete(registerHoras);

        // When
        registerHorasService.DeleteRegisteredHoursUser(uuid);

        // Then
        verify(registroHorasRepository).findById(uuid);
        verify(registroHorasRepository).delete(registerHoras);
    }


// ... existing code ...

    @Test
    @DisplayName("Deve lançar exceção ao deletar registro inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentRegister() {
        // Given
        UUID uuid = UUID.randomUUID();
        when(registroHorasRepository.findById(uuid)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> registerHorasService.DeleteRegisteredHoursUser(uuid))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Registro não encontrado");

        verify(registroHorasRepository).findById(uuid);
        verify(registroHorasRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve buscar registros paginados por usuário")
    void shouldFindRegisteredHoursByUserPaginated() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<RegisterHoras> page = new PageImpl<>(List.of(registerHoras), pageable, 1);

        when(registroHorasRepository.findByEstagiario("neto", pageable)).thenReturn(page);
        when(requestMapper.mapToListRegisterResponse(anyList()))
                .thenReturn(List.of(registerResponse));

        // When
        PageResponse<RegisterResponse> result =
                registerHorasService.findAllRegisteredHoursUser("neto", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(1);
        assertThat(result.first()).isTrue();
        assertThat(result.last()).isTrue();

        verify(registroHorasRepository).findByEstagiario("neto", pageable);
    }

    @Test
    @DisplayName("Deve calcular horas quando horas trabalhadas é zero")
    void shouldCalculateHoursWhenHoursIsZero() {
        // Given
        RegisterRequest requestComZeroHoras = new RegisterRequest(
                "neto",
                "Desenvolvimento",
                LocalDateTime.of(2024, 1, 15, 9, 0),
                LocalDateTime.of(2024, 1, 15, 17, 0),
                0
        );

        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));
        when(requestMapper.mapToRegisterHoras(requestComZeroHoras)).thenReturn(registerHoras);
        when(registroHorasRepository.save(any(RegisterHoras.class))).thenReturn(registerHoras);
        when(requestMapper.mapRegisterResponse(any())).thenReturn(registerResponse);

        // When
        registerHorasService.submitHours(requestComZeroHoras);

        // Then
        verify(registroHorasRepository).save(argThat(reg -> reg.getHorasTrabalhadas() == 8));
    }

    @Test
    @DisplayName("Deve calcular horas quando horas trabalhadas é negativo")
    void shouldCalculateHoursWhenHoursIsNegative() {
        // Given
        RegisterRequest requestComHorasNegativas = new RegisterRequest(
                "neto",
                "Desenvolvimento",
                LocalDateTime.of(2024, 1, 15, 9, 0),
                LocalDateTime.of(2024, 1, 15, 18, 0),
                -1
        );

        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));
        when(requestMapper.mapToRegisterHoras(requestComHorasNegativas)).thenReturn(registerHoras);
        when(registroHorasRepository.save(any(RegisterHoras.class))).thenReturn(registerHoras);
        when(requestMapper.mapRegisterResponse(any())).thenReturn(registerResponse);

        // When
        registerHorasService.submitHours(requestComHorasNegativas);

        // Then
        verify(registroHorasRepository).save(argThat(reg -> reg.getHorasTrabalhadas() == 9));
    }

    @Test
    @DisplayName("Deve lançar exceção quando data fim é anterior à data início")
    void shouldThrowExceptionWhenEndDateIsBeforeStartDate() {
        // Given
        RegisterRequest requestComDatasInvalidas = new RegisterRequest(
                "neto",
                "Desenvolvimento",
                LocalDateTime.of(2024, 1, 15, 18, 0),
                LocalDateTime.of(2024, 1, 15, 9, 0), // Data fim antes da data início
                0
        );

        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));
        when(requestMapper.mapToRegisterHoras(requestComDatasInvalidas)).thenReturn(registerHoras);

        // When & Then
        assertThatThrownBy(() -> registerHorasService.submitHours(requestComDatasInvalidas))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Data fim não pode ser anterior à data início");

        verify(registroHorasRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve usar horas fornecidas quando maior que zero")
    void shouldUseProvidedHoursWhenGreaterThanZero() {
        // Given
        RegisterRequest requestComHorasFornecidas = new RegisterRequest(
                "neto",
                "Desenvolvimento",
                LocalDateTime.of(2024, 1, 15, 9, 0),
                LocalDateTime.of(2024, 1, 15, 18, 0),
                8 // Horas fornecidas
        );

        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));
        when(requestMapper.mapToRegisterHoras(requestComHorasFornecidas)).thenReturn(registerHoras);
        when(registroHorasRepository.save(any(RegisterHoras.class))).thenReturn(registerHoras);
        when(requestMapper.mapRegisterResponse(any())).thenReturn(registerResponse);

        // When
        registerHorasService.submitHours(requestComHorasFornecidas);

        // Then
        verify(registroHorasRepository).save(argThat(reg -> reg.getHorasTrabalhadas() == 8));
    }

    @Test
    @DisplayName("Deve atualizar registro alterando o estagiário")
    void shouldUpdateRegisterChangingEstagiario() {
        // Given
        UUID uuid = UUID.randomUUID();
        Usuario novoUsuario = Usuario.builder()
                .id(UUID.randomUUID())
                .username("admin")
                .password("senha")
                .role("ROLE_ADMIN")
                .build();

        RegisterRequest requestComNovoEstagiario = new RegisterRequest(
                "admin", // Estagiário diferente
                "Nova descrição",
                LocalDateTime.of(2024, 1, 16, 9, 0),
                LocalDateTime.of(2024, 1, 16, 17, 0),
                8
        );

        when(registroHorasRepository.findById(uuid)).thenReturn(Optional.of(registerHoras));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(novoUsuario));
        when(registroHorasRepository.save(any(RegisterHoras.class))).thenReturn(registerHoras);
        when(requestMapper.mapRegisterResponse(any())).thenReturn(registerResponse);

        // When
        registerHorasService.updateRegister(uuid, requestComNovoEstagiario);

        // Then
        verify(usuarioRepository).findByUsername("admin");
        verify(registroHorasRepository).save(argThat(reg ->
                reg.getEstagiario().equals("admin") &&
                        reg.getUsuario().equals(novoUsuario)
        ));
    }

    @Test
    @DisplayName("Deve recalcular horas ao atualizar com horas zero")
    void shouldRecalculateHoursWhenUpdatingWithZeroHours() {
        // Given
        UUID uuid = UUID.randomUUID();
        RegisterRequest requestComZeroHoras = new RegisterRequest(
                "neto",
                "Descrição atualizada",
                LocalDateTime.of(2024, 1, 15, 9, 0),
                LocalDateTime.of(2024, 1, 15, 14, 0),
                0 // Força recálculo
        );

        when(registroHorasRepository.findById(uuid)).thenReturn(Optional.of(registerHoras));
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));
        when(registroHorasRepository.save(any(RegisterHoras.class))).thenReturn(registerHoras);
        when(requestMapper.mapRegisterResponse(any())).thenReturn(registerResponse);

        // When
        registerHorasService.updateRegister(uuid, requestComZeroHoras);

        // Then
        verify(registroHorasRepository).save(argThat(reg -> reg.getHorasTrabalhadas() == 5));
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há registros")
    void shouldReturnEmptyListWhenNoRegisters() {
        // Given
        when(registroHorasRepository.findAll()).thenReturn(List.of());
        when(requestMapper.mapToListRegisterResponse(anyList())).thenReturn(List.of());

        // When
        List<RegisterResponse> result = registerHorasService.findAllRegisteredHours();

        // Then
        assertThat(result).isEmpty();
        verify(registroHorasRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tem registros")
    void shouldReturnEmptyListWhenUserHasNoRegisters() {
        // Given
        when(registroHorasRepository.findByEstagiario("inexistente")).thenReturn(List.of());
        when(requestMapper.mapToListRegisterResponse(anyList())).thenReturn(List.of());

        // When
        List<RegisterResponse> result = registerHorasService.findAllRegisteredHoursUser("inexistente");

        // Then
        assertThat(result).isEmpty();
        verify(registroHorasRepository).findByEstagiario("inexistente");
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há registros")
    void shouldReturnEmptyPageWhenNoRegisters() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<RegisterHoras> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(registroHorasRepository.findAll(pageable)).thenReturn(emptyPage);
        when(requestMapper.mapToListRegisterResponse(anyList())).thenReturn(List.of());

        // When
        PageResponse<RegisterResponse> result = registerHorasService.findAllRegisteredHours(pageable);

        // Then
        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
        assertThat(result.first()).isTrue();
        assertThat(result.last()).isTrue();
    }

    @Test
    @DisplayName("Deve calcular corretamente horas maiores que 24")
    void shouldCalculateHoursGreaterThan24Correctly() {
        // Given
        RegisterRequest requestComMuitasHoras = new RegisterRequest(
                "neto",
                "Desenvolvimento",
                LocalDateTime.of(2024, 1, 15, 9, 0),
                LocalDateTime.of(2024, 1, 16, 12, 0), // 27 horas depois
                0
        );

        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));
        when(requestMapper.mapToRegisterHoras(requestComMuitasHoras)).thenReturn(registerHoras);
        when(registroHorasRepository.save(any(RegisterHoras.class))).thenReturn(registerHoras);
        when(requestMapper.mapRegisterResponse(any())).thenReturn(registerResponse);

        // When
        registerHorasService.submitHours(requestComMuitasHoras);

        // Then
        verify(registroHorasRepository).save(argThat(reg -> reg.getHorasTrabalhadas() == 27));
    }

    @Test
    @DisplayName("Deve atualizar mantendo o mesmo estagiário")
    void shouldUpdateKeepingSameEstagiario() {
        // Given
        UUID uuid = UUID.randomUUID();
        RegisterRequest requestMesmoEstagiario = new RegisterRequest(
                "neto", // Mesmo estagiário
                "Nova descrição",
                LocalDateTime.of(2024, 1, 16, 9, 0),
                LocalDateTime.of(2024, 1, 16, 17, 0),
                8
        );

        when(registroHorasRepository.findById(uuid)).thenReturn(Optional.of(registerHoras));
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));
        when(registroHorasRepository.save(any(RegisterHoras.class))).thenReturn(registerHoras);
        when(requestMapper.mapRegisterResponse(any())).thenReturn(registerResponse);

        // When
        registerHorasService.updateRegister(uuid, requestMesmoEstagiario);

        // Then
        // Não deve buscar novo usuário pois o estagiário é o mesmo
        verify(usuarioRepository, never()).findByUsername("neto");
        verify(registroHorasRepository).save(any(RegisterHoras.class));
    }

    @Test
    @DisplayName("Deve atualizar com horas fornecidas sem recalcular")
    void shouldUpdateWithProvidedHoursWithoutRecalculating() {
        // Given
        UUID uuid = UUID.randomUUID();
        RegisterRequest requestComHoras = new RegisterRequest(
                "neto",
                "Descrição",
                LocalDateTime.of(2024, 1, 15, 9, 0),
                LocalDateTime.of(2024, 1, 15, 17, 0),
                7 // Horas fornecidas (diferente do calculado que seria 8)
        );

        when(registroHorasRepository.findById(uuid)).thenReturn(Optional.of(registerHoras));
        when(usuarioRepository.findByUsername("neto")).thenReturn(Optional.of(usuario));
        when(registroHorasRepository.save(any(RegisterHoras.class))).thenReturn(registerHoras);
        when(requestMapper.mapRegisterResponse(any())).thenReturn(registerResponse);

        // When
        registerHorasService.updateRegister(uuid, requestComHoras);

        // Then
        verify(registroHorasRepository).save(argThat(reg -> reg.getHorasTrabalhadas() == 7));
    }

    @Test
    @DisplayName("Deve buscar múltiplos registros paginados")
    void shouldFindMultipleRegistersPaginated() {
        // Given
        RegisterHoras registro2 = RegisterHoras.builder()
                .id(UUID.randomUUID())
                .estagiario("neto")
                .descricao("Outra tarefa")
                .dataInicio(LocalDateTime.of(2024, 1, 16, 9, 0))
                .dataFim(LocalDateTime.of(2024, 1, 16, 17, 0))
                .horasTrabalhadas(8)
                .usuario(usuario)
                .build();

        RegisterResponse response2 = new RegisterResponse(
                "neto",
                "Outra tarefa",
                LocalDateTime.of(2024, 1, 16, 9, 0),
                LocalDateTime.of(2024, 1, 16, 17, 0),
                8
        );

        Pageable pageable = PageRequest.of(0, 10);
        Page<RegisterHoras> page = new PageImpl<>(
                List.of(registerHoras, registro2),
                pageable,
                2
        );

        when(registroHorasRepository.findAll(pageable)).thenReturn(page);
        when(requestMapper.mapToListRegisterResponse(anyList()))
                .thenReturn(List.of(registerResponse, response2));

        // When
        PageResponse<RegisterResponse> result = registerHorasService.findAllRegisteredHours(pageable);

        // Then
        assertThat(result.content()).hasSize(2);
        assertThat(result.totalElements()).isEqualTo(2);
        assertThat(result.first()).isTrue();
        assertThat(result.last()).isTrue();
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar com novo usuário inexistente")
    void shouldThrowExceptionWhenUpdatingWithNonExistentNewUser() {
        // Given
        UUID uuid = UUID.randomUUID();
        RegisterRequest requestComUsuarioInexistente = new RegisterRequest(
                "inexistente",
                "Descrição",
                LocalDateTime.of(2024, 1, 15, 9, 0),
                LocalDateTime.of(2024, 1, 15, 17, 0),
                8
        );

        when(registroHorasRepository.findById(uuid)).thenReturn(Optional.of(registerHoras));
        when(usuarioRepository.findByUsername("inexistente")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> registerHorasService.updateRegister(uuid, requestComUsuarioInexistente))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuário não encontrado");

        verify(registroHorasRepository, never()).save(any());
    }
}
