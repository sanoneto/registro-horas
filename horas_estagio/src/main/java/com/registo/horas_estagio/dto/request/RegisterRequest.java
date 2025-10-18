package com.registo.horas_estagio.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Request para registrar horas")
public record RegisterRequest(

        @NotBlank(message = "O o nome estagiario é obrigatório")
        @Size(min = 2, max = 50, message = "Nome deve ter entre 2 e 50 caracteres")
        @Pattern(regexp = "^[A-Za-zÀ-ÿ\\s]+$", message = "Nome deve conter apenas letras")
        String estagiario,

        @Schema(
                description = "Descrição da atividade",
                example = "Desenvolvimento de API REST",
                required = true
        )
        @NotBlank(message = "O primeiro nome é obrigatório")
        @Size(min = 2, max = 50, message = "Nome deve ter entre 2 e 50 caracteres")
        String descricao,

        @Schema(
                description = "Data e hora de início",
                example = "2024-01-15T09:00:00",
                required = true,
                type = "string",
                format = "date-time"
        )
        @NotNull(message = "Data início é obrigatória")
        LocalDateTime dataInicio,
        @Schema(
                description = "Data e hora de término",
                example = "2024-01-15T18:00:00",
                required = true,
                type = "string",
                format = "date-time"
        )
        @NotNull(message = "Data fim é obrigatória")
        LocalDateTime dataFim,
        @Schema(
                description = "Data e hora trabalhadas",
                example = "4",
                required = true,
                type = "string"
        )
        int horasTrabalhadas

) {
}
