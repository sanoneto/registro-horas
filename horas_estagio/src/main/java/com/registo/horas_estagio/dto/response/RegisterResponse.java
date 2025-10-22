package com.registo.horas_estagio.dto.response;


import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterResponse(
        UUID publicId,
        String estagiario,
        String descricao,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
        double horasTrabalhadas
) {
}
