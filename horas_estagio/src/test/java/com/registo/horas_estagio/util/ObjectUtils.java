package com.registo.horas_estagio.util;

import com.registo.horas_estagio.dto.request.RegisterRequest;
import com.registo.horas_estagio.dto.response.RegisterResponse;
import com.registo.horas_estagio.models.RegisterHoras;
import com.registo.horas_estagio.models.Usuario;

import java.time.LocalDateTime;
import java.util.UUID;

public class ObjectUtils {

    public static final String ESTAGIARIO_PADRAO = "neto";
    public static final String DESCRICAO_PADRAO = "Desenvolvimento de API REST";
    public static final LocalDateTime DATA_INICIO_PADRAO = LocalDateTime.of(2024, 1, 15, 9, 0);
    public static final LocalDateTime DATA_FIM_PADRAO = LocalDateTime.of(2024, 1, 15, 18, 0);
    public static final Integer HORAS_PADRAO = 9;

    public static RegisterRequest createDefaultRequest() {
        return new RegisterRequest(
                ESTAGIARIO_PADRAO,
                DESCRICAO_PADRAO,
                DATA_INICIO_PADRAO,
                DATA_FIM_PADRAO,
                HORAS_PADRAO
        );
    }

    public static RegisterResponse createDefaultResponse() {
        return new RegisterResponse(
                UUID.randomUUID(),
                ESTAGIARIO_PADRAO,
                DESCRICAO_PADRAO,
                DATA_INICIO_PADRAO,
                DATA_FIM_PADRAO,
                HORAS_PADRAO
        );
    }

    public static Usuario createDefaultUsuario() {
        return Usuario.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .username(ESTAGIARIO_PADRAO)
                .password("senha123")
                .role("ROLE_ESTAGIARIO")
                .build();
    }

    public static RegisterHoras createDefaultRegisterHoras() {
        Usuario usuario = createDefaultUsuario();
        return RegisterHoras.builder()
                .id(1L)
                .publicId(UUID.randomUUID())
                .estagiario(ESTAGIARIO_PADRAO)
                .descricao(DESCRICAO_PADRAO)
                .dataInicio(DATA_INICIO_PADRAO)
                .dataFim(DATA_FIM_PADRAO)
                .horasTrabalhadas(HORAS_PADRAO)
                .usuario(usuario)
                .build();
    }

}
