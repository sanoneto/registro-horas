package com.registo.horas_estagio.controller;

import com.registo.horas_estagio.dto.request.RegisterRequest;
import com.registo.horas_estagio.dto.response.PageResponse;
import com.registo.horas_estagio.dto.response.RegisterResponse;
import com.registo.horas_estagio.service.RegisterHorasService;
import com.registo.horas_estagio.util.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários do RegistroHorasController")
class RegistroHorasControllerUnitTest {

    @Mock
    private RegisterHorasService registerHorasService;

    @InjectMocks
    private RegistroHorasController controller;

    private RegisterRequest registerRequest;
    private RegisterResponse registerResponse;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        registerRequest = ObjectUtils.createDefaultRequest();
        registerResponse = ObjectUtils.createDefaultResponse();
        testUuid = UUID.randomUUID();
    }

    // ==================== TESTES GET /api/registos/list ====================

    @Test
    @DisplayName("Deve retornar lista de todos os registros com sucesso")
    void shouldGetAllRegisterHorasSuccessfully() {
        // Given
        List<RegisterResponse> expectedList = List.of(registerResponse);
        when(registerHorasService.findAllRegisteredHours()).thenReturn(expectedList);

        // When
        ResponseEntity<List<RegisterResponse>> response = controller.getAllRegisterHoras();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).estagiario()).isEqualTo("neto");

        verify(registerHorasService).findAllRegisteredHours();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há registros")
    void shouldReturnEmptyListWhenNoRegisters() {
        // Given
        when(registerHorasService.findAllRegisteredHours()).thenReturn(List.of());

        // When
        ResponseEntity<List<RegisterResponse>> response = controller.getAllRegisterHoras();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();

        verify(registerHorasService).findAllRegisteredHours();
    }

    @Test
    @DisplayName("Deve retornar múltiplos registros")
    void shouldReturnMultipleRegisters() {
        // Given
        RegisterResponse response2 = new RegisterResponse(
                UUID.randomUUID(),
                "admin",
                "Outra tarefa",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(5),
                5
        );
        List<RegisterResponse> expectedList = List.of(registerResponse, response2);
        when(registerHorasService.findAllRegisteredHours()).thenReturn(expectedList);

        // When
        ResponseEntity<List<RegisterResponse>> response = controller.getAllRegisterHoras();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);

        verify(registerHorasService).findAllRegisteredHours();
    }

    // ==================== TESTES GET /api/registos/list/paginated ====================

    @Test
    @DisplayName("Deve retornar registros paginados com parâmetros padrão")
    void shouldGetPaginatedRegisterHorasWithDefaultParams() {
        // Given
        PageResponse<RegisterResponse> pageResponse = PageResponse.of(
                List.of(registerResponse),
                0, 10, 1, 1, true, true
        );
        when(registerHorasService.findAllRegisteredHours(any(Pageable.class)))
                .thenReturn(pageResponse);

        // When
        ResponseEntity<PageResponse<RegisterResponse>> response =
                controller.getAllRegisterHorasPaginated(0, 10, "dataInicio", "DESC");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).hasSize(1);
        assertThat(response.getBody().totalElements()).isEqualTo(1);
        assertThat(response.getBody().first()).isTrue();
        assertThat(response.getBody().last()).isTrue();

        verify(registerHorasService).findAllRegisteredHours(any(Pageable.class));
    }

    @Test
    @DisplayName("Deve retornar página com ordenação ASC")
    void shouldGetPaginatedRegisterHorasWithAscOrder() {
        // Given
        PageResponse<RegisterResponse> pageResponse = PageResponse.of(
                List.of(registerResponse),
                0, 5, 1, 1, true, true
        );
        when(registerHorasService.findAllRegisteredHours(any(Pageable.class)))
                .thenReturn(pageResponse);

        // When
        ResponseEntity<PageResponse<RegisterResponse>> response =
                controller.getAllRegisterHorasPaginated(0, 5, "estagiario", "ASC");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        verify(registerHorasService).findAllRegisteredHours(
                argThat(pageable ->
                        pageable.getPageNumber() == 0 &&
                                pageable.getPageSize() == 5 &&
                                pageable.getSort().getOrderFor("estagiario") != null &&
                                pageable.getSort().getOrderFor("estagiario").getDirection() == Sort.Direction.ASC
                )
        );
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há registros")
    void shouldReturnEmptyPageWhenNoRegisters() {
        // Given
        PageResponse<RegisterResponse> emptyPage = PageResponse.of(
                List.of(),
                0, 10, 0, 0, true, true
        );
        when(registerHorasService.findAllRegisteredHours(any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        ResponseEntity<PageResponse<RegisterResponse>> response =
                controller.getAllRegisterHorasPaginated(0, 10, "dataInicio", "DESC");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).isEmpty();
        assertThat(response.getBody().totalElements()).isZero();

        verify(registerHorasService).findAllRegisteredHours(any(Pageable.class));
    }

    @Test
    @DisplayName("Deve lidar com diferentes páginas")
    void shouldHandleDifferentPages() {
        // Given
        PageResponse<RegisterResponse> pageResponse = PageResponse.of(
                List.of(registerResponse),
                2, 10, 25, 3, false, false
        );
        when(registerHorasService.findAllRegisteredHours(any(Pageable.class)))
                .thenReturn(pageResponse);

        // When
        ResponseEntity<PageResponse<RegisterResponse>> response =
                controller.getAllRegisterHorasPaginated(2, 10, "dataInicio", "DESC");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().pageNumber()).isEqualTo(2);
        assertThat(response.getBody().first()).isFalse();
        assertThat(response.getBody().last()).isFalse();

        verify(registerHorasService).findAllRegisteredHours(any(Pageable.class));
    }

    // ==================== TESTES GET /api/registos/list/{name} ====================

    @Test
    @DisplayName("Deve retornar registros de um usuário específico")
    void shouldGetRegisterHorasByUser() {
        // Given
        when(registerHorasService.findAllRegisteredHoursUser("neto"))
                .thenReturn(List.of(registerResponse));

        // When
        ResponseEntity<List<RegisterResponse>> response =
                controller.getAllRegisterHorasUser("neto");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).estagiario()).isEqualTo("neto");

        verify(registerHorasService).findAllRegisteredHoursUser("neto");
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando usuário não tem registros")
    void shouldReturnEmptyListWhenUserHasNoRegisters() {
        // Given
        when(registerHorasService.findAllRegisteredHoursUser("inexistente"))
                .thenReturn(List.of());

        // When
        ResponseEntity<List<RegisterResponse>> response =
                controller.getAllRegisterHorasUser("inexistente");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();

        verify(registerHorasService).findAllRegisteredHoursUser("inexistente");
    }

    // ==================== TESTES GET /api/registos/list/{name}/paginated ====================

    @Test
    @DisplayName("Deve retornar registros paginados de um usuário")
    void shouldGetPaginatedRegisterHorasByUser() {
        // Given
        PageResponse<RegisterResponse> pageResponse = PageResponse.of(
                List.of(registerResponse),
                0, 10, 1, 1, true, true
        );
        when(registerHorasService.findAllRegisteredHoursUser(eq("neto"), any(Pageable.class)))
                .thenReturn(pageResponse);

        // When
        ResponseEntity<PageResponse<RegisterResponse>> response =
                controller.getAllRegisterHorasUserPaginated("neto", 0, 10, "dataInicio", "DESC");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).hasSize(1);
        assertThat(response.getBody().content().get(0).estagiario()).isEqualTo("neto");

        verify(registerHorasService).findAllRegisteredHoursUser(eq("neto"), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve retornar página vazia quando usuário não tem registros")
    void shouldReturnEmptyPageWhenUserHasNoRegistersPaginated() {
        // Given
        PageResponse<RegisterResponse> emptyPage = PageResponse.of(
                List.of(),
                0, 10, 0, 0, true, true
        );
        when(registerHorasService.findAllRegisteredHoursUser(eq("inexistente"), any(Pageable.class)))
                .thenReturn(emptyPage);

        // When
        ResponseEntity<PageResponse<RegisterResponse>> response =
                controller.getAllRegisterHorasUserPaginated("inexistente", 0, 10, "dataInicio", "DESC");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().content()).isEmpty();

        verify(registerHorasService).findAllRegisteredHoursUser(eq("inexistente"), any(Pageable.class));
    }

    @Test
    @DisplayName("Deve respeitar parâmetros de paginação customizados para usuário")
    void shouldRespectCustomPaginationParamsForUser() {
        // Given
        PageResponse<RegisterResponse> pageResponse = PageResponse.of(
                List.of(registerResponse),
                1, 5, 10, 2, false, false
        );
        when(registerHorasService.findAllRegisteredHoursUser(eq("neto"), any(Pageable.class)))
                .thenReturn(pageResponse);

        // When
        ResponseEntity<PageResponse<RegisterResponse>> response =
                controller.getAllRegisterHorasUserPaginated("neto", 1, 5, "descricao", "ASC");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().pageNumber()).isEqualTo(1);
        assertThat(response.getBody().pageSize()).isEqualTo(5);

        verify(registerHorasService).findAllRegisteredHoursUser(
                eq("neto"),
                argThat(pageable ->
                        pageable.getPageNumber() == 1 &&
                                pageable.getPageSize() == 5 &&
                                pageable.getSort().getOrderFor("descricao") != null &&
                                Objects.requireNonNull(pageable.getSort().getOrderFor("descricao")).getDirection() == Sort.Direction.ASC
                )
        );
    }

    // ==================== TESTES POST /api/registos/add ====================

    @Test
    @DisplayName("Deve criar novo registro com sucesso")
    void shouldCreateRegisterSuccessfully() {
        // Given
        when(registerHorasService.submitHours(any(RegisterRequest.class)))
                .thenReturn(registerResponse);

        // When
        ResponseEntity<RegisterResponse> response = controller.addRegisterHoras(registerRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().estagiario()).isEqualTo("neto");
        assertThat(response.getBody().descricao()).isEqualTo("Desenvolvimento de API REST");

        verify(registerHorasService).submitHours(registerRequest);
    }

    @Test
    @DisplayName("Deve criar registro com dados mínimos")
    void shouldCreateRegisterWithMinimalData() {
        // Given
        RegisterRequest minimalRequest = new RegisterRequest(
                UUID.randomUUID(),
                "neto",
                "Tarefa",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                1
        );
        RegisterResponse minimalResponse = new RegisterResponse(
                UUID.randomUUID(),
                "neto",
                "Tarefa",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                1
        );
        when(registerHorasService.submitHours(any(RegisterRequest.class)))
                .thenReturn(minimalResponse);

        // When
        ResponseEntity<RegisterResponse> response = controller.addRegisterHoras(minimalRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().horasTrabalhadas()).isEqualTo(1);

        verify(registerHorasService).submitHours(minimalRequest);
    }

    // ==================== TESTES PUT /api/registos/update/{uuid} ====================

    @Test
    @DisplayName("Deve atualizar registro com sucesso")
    void shouldUpdateRegisterSuccessfully() {
        // Given
        RegisterResponse updatedResponse = new RegisterResponse(
                testUuid,
                "neto",
                "Descrição atualizada",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(8),
                8
        );
        when(registerHorasService.updateRegister(eq(testUuid), any(RegisterRequest.class)))
                .thenReturn(updatedResponse);

        // When
        ResponseEntity<RegisterResponse> response =
                controller.updateRegister(testUuid, registerRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().publicId()).isEqualTo(testUuid);
        assertThat(response.getBody().descricao()).isEqualTo("Descrição atualizada");

        verify(registerHorasService).updateRegister(testUuid, registerRequest);
    }

    @Test
    @DisplayName("Deve atualizar todos os campos do registro")
    void shouldUpdateAllRegisterFields() {
        // Given
        LocalDateTime novaDataInicio = LocalDateTime.of(2024, 2, 1, 10, 0);
        LocalDateTime novaDataFim = LocalDateTime.of(2024, 2, 1, 18, 0);

        RegisterRequest updateRequest = new RegisterRequest(
                testUuid,
                "admin",
                "Nova descrição completa",
                novaDataInicio,
                novaDataFim,
                8
        );

        RegisterResponse updatedResponse = new RegisterResponse(
                testUuid,
                "admin",
                "Nova descrição completa",
                novaDataInicio,
                novaDataFim,
                8
        );

        when(registerHorasService.updateRegister(eq(testUuid), any(RegisterRequest.class)))
                .thenReturn(updatedResponse);

        // When
        ResponseEntity<RegisterResponse> response =
                controller.updateRegister(testUuid, updateRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().estagiario()).isEqualTo("admin");
        assertThat(response.getBody().descricao()).isEqualTo("Nova descrição completa");
        assertThat(response.getBody().horasTrabalhadas()).isEqualTo(8);

        verify(registerHorasService).updateRegister(testUuid, updateRequest);
    }

    // ==================== TESTES DELETE /api/registos/delete/{publicId} ====================

    @Test
    @DisplayName("Deve deletar registro com sucesso")
    void shouldDeleteRegisterSuccessfully() {
        // Given
        doNothing().when(registerHorasService).DeleteRegisteredHoursUser(testUuid);

        // When
        ResponseEntity<Void> response = controller.deleteRegister(testUuid);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(registerHorasService).DeleteRegisteredHoursUser(testUuid);
    }

    @Test
    @DisplayName("Deve chamar serviço de deleção com UUID correto")
    void shouldCallDeleteServiceWithCorrectUuid() {
        // Given
        UUID specificUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        doNothing().when(registerHorasService).DeleteRegisteredHoursUser(specificUuid);

        // When
        controller.deleteRegister(specificUuid);

        // Then
        verify(registerHorasService).DeleteRegisteredHoursUser(specificUuid);
        verify(registerHorasService, times(1)).DeleteRegisteredHoursUser(any(UUID.class));
    }

    // ==================== TESTES DE CENÁRIOS EDGE CASES ====================

    @Test
    @DisplayName("Deve lidar com direção de ordenação em minúsculas")
    void shouldHandleLowercaseSortDirection() {
        // Given
        PageResponse<RegisterResponse> pageResponse = PageResponse.of(
                List.of(registerResponse),
                0, 10, 1, 1, true, true
        );
        when(registerHorasService.findAllRegisteredHours(any(Pageable.class)))
                .thenReturn(pageResponse);

        // When
        ResponseEntity<PageResponse<RegisterResponse>> response =
                controller.getAllRegisterHorasPaginated(0, 10, "dataInicio", "asc");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(registerHorasService).findAllRegisteredHours(
                argThat(pageable ->
                        Objects.requireNonNull(pageable.getSort().getOrderFor("dataInicio")).getDirection() == Sort.Direction.ASC
                )
        );
    }

    @Test
    @DisplayName("Deve usar DESC como padrão para direção inválida")
    void shouldUseDescAsDefaultForInvalidDirection() {
        // Given
        PageResponse<RegisterResponse> pageResponse = PageResponse.of(
                List.of(registerResponse),
                0, 10, 1, 1, true, true
        );
        when(registerHorasService.findAllRegisteredHours(any(Pageable.class)))
                .thenReturn(pageResponse);

        // When
        ResponseEntity<PageResponse<RegisterResponse>> response =
                controller.getAllRegisterHorasPaginated(0, 10, "dataInicio", "INVALID");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        verify(registerHorasService).findAllRegisteredHours(
                argThat(pageable ->
                        Objects.requireNonNull(pageable.getSort().getOrderFor("dataInicio")).getDirection() == Sort.Direction.DESC
                )
        );
    }

    @Test
    @DisplayName("Deve processar nome de usuário com caracteres especiais")
    void shouldHandleUsernameWithSpecialCharacters() {
        // Given
        String specialUsername = "user.name-123";
        when(registerHorasService.findAllRegisteredHoursUser(specialUsername))
                .thenReturn(List.of(registerResponse));

        // When
        ResponseEntity<List<RegisterResponse>> response =
                controller.getAllRegisterHorasUser(specialUsername);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(registerHorasService).findAllRegisteredHoursUser(specialUsername);
    }

    @Test
    @DisplayName("Deve criar registro mantendo o UUID fornecido no request")
    void shouldCreateRegisterKeepingProvidedUuid() {
        // Given
        UUID providedUuid = UUID.randomUUID();
        RegisterRequest requestWithUuid = new RegisterRequest(
                providedUuid,
                "neto",
                "Tarefa",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(5),
                5
        );
        RegisterResponse responseWithUuid = new RegisterResponse(
                providedUuid,
                "neto",
                "Tarefa",
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(5),
                5
        );

        when(registerHorasService.submitHours(any(RegisterRequest.class)))
                .thenReturn(responseWithUuid);

        // When
        ResponseEntity<RegisterResponse> response = controller.addRegisterHoras(requestWithUuid);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().publicId()).isEqualTo(providedUuid);

        verify(registerHorasService).submitHours(requestWithUuid);
    }
}