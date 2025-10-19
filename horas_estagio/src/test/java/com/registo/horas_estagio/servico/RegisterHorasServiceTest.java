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
}