package com.registo.horas_estagio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.registo.horas_estagio.config.TestSecurityConfig;
import com.registo.horas_estagio.dto.request.RegisterRequest;
import com.registo.horas_estagio.dto.response.PageResponse;
import com.registo.horas_estagio.dto.response.RegisterResponse;
import com.registo.horas_estagio.service.RegisterHorasService;
import com.registo.horas_estagio.util.ObjectUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("Testes do RegistroHorasController")
class RegistroHorasControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegisterHorasService registerHorasService;

    private RegisterRequest registerRequest;
    private RegisterResponse registerResponse;

    @BeforeEach
    void setUp() {
        registerRequest = ObjectUtils.createDefaultRequest();
        registerResponse = ObjectUtils.createDefaultResponse();
    }

    @Test
    @DisplayName("Deve listar todos os registros (ADMIN)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetAllRegisterHorasAsAdmin() throws Exception {
        // Given
        when(registerHorasService.findAllRegisteredHours())
                .thenReturn(List.of(registerResponse));

        // When & Then
        mockMvc.perform(get("/api/registos/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].estagiario").value("neto"))
                .andExpect(jsonPath("$[0].horasTrabalhadas").value(9));

        verify(registerHorasService).findAllRegisteredHours();
    }

    @Test
    @DisplayName("Deve negar acesso para não-ADMIN")
    @WithMockUser(username = "user", roles = {"ESTAGIARIO"})
    void shouldDenyAccessForNonAdmin() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/registos/list"))
                .andExpect(status().isForbidden());

        verify(registerHorasService, never()).findAllRegisteredHours();
    }

    @Test
    @DisplayName("Deve listar registros paginados (ADMIN)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldGetPaginatedRegisterHoras() throws Exception {
        // Given
        PageResponse<RegisterResponse> pageResponse = PageResponse.of(
                List.of(registerResponse),
                0, 10, 1, 1, true, true
        );

        when(registerHorasService.findAllRegisteredHours(any(Pageable.class)))
                .thenReturn(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/registos/list/paginated")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.first").value(true));

        verify(registerHorasService).findAllRegisteredHours(any(Pageable.class));
    }

    @Test
    @DisplayName("Deve criar novo registro (ADMIN)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCreateRegisterAsAdmin() throws Exception {
        // Given
        when(registerHorasService.submitHours(any(RegisterRequest.class)))
                .thenReturn(registerResponse);

        // When & Then
        mockMvc.perform(post("/api/registos/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estagiario").value("neto"))
                .andExpect(jsonPath("$.horasTrabalhadas").value(9));

        verify(registerHorasService).submitHours(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Deve listar registros do próprio usuário (ESTAGIARIO)")
    @WithMockUser(username = "neto", roles = {"ESTAGIARIO"})
    void shouldGetOwnRegisterAsEstagiario() throws Exception {
        // Given
        when(registerHorasService.findAllRegisteredHoursUser("neto"))
                .thenReturn(List.of(registerResponse));

        // When & Then
        mockMvc.perform(get("/api/registos/list/neto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estagiario").value("neto"));

        verify(registerHorasService).findAllRegisteredHoursUser("neto");
    }

    @Test
    @DisplayName("Deve atualizar registro (ADMIN)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldUpdateRegisterAsAdmin() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        when(registerHorasService.updateRegister(any(UUID.class), any(RegisterRequest.class)))
                .thenReturn(registerResponse);

        // When & Then
        mockMvc.perform(put("/api/registos/update/" + uuid)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estagiario").value("neto"));

        verify(registerHorasService).updateRegister(any(UUID.class), any(RegisterRequest.class));
    }

    @Test
    @DisplayName("Deve deletar registro (ADMIN)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldDeleteRegisterAsAdmin() throws Exception {
        // Given
        UUID uuid = UUID.randomUUID();
        doNothing().when(registerHorasService).DeleteRegisteredHoursUser(uuid);

        // When & Then
        mockMvc.perform(delete("/api/registos/delete/" + uuid)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(registerHorasService).DeleteRegisteredHoursUser(uuid);
    }

    @Test
    @DisplayName("Deve validar campos obrigatórios")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldValidateRequiredFields() throws Exception {
        // Given
        RegisterRequest invalidRequest = new RegisterRequest(
                "",  // estagiario vazio
                "",  // descrição vazia
                null,  // dataInicio null
                null,  // dataFim null
                0
        );

        // When & Then
        mockMvc.perform(post("/api/registos/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(registerHorasService, never()).submitHours(any());
    }
}