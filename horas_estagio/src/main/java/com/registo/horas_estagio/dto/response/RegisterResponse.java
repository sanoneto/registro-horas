package com.registo.horas_estagio.dto.response;


import java.time.LocalDateTime;

public record RegisterResponse(
        String estagiario,
        String descricao,
        LocalDateTime dataInicio,
        LocalDateTime dataFim,
     int horasTrabalhadas
) {
}
