package com.registo.horas_estagio.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Request de login do usuário")
public record LoginRequest(
        @Schema(
                description = " teu username ",
                example = "neto",
                required = true
        )
        @NotBlank(message = "O primeiro nome é obrigatório")
        @Size(min = 2, max = 50, message = "Nome deve ter entre 2 e 50 caracteres")
        @Pattern(regexp = "^[A-Za-zÀ-ÿ\\s]+$", message = "Nome deve conter apenas letras")
        String username,

        @Schema(
                description = "Senha do utilizador",
                example = "3423234",
                required = true
        )
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
       String password) {
}
