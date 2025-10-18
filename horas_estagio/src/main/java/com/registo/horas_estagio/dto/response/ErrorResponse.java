package com.registo.horas_estagio.dto.response;

public record ErrorResponse(
        String message,
        int status
) {
}