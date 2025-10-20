package com.registo.horas_estagio.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request para create User")
public record UserCredentialsRequest(

         UUID publicId,

        @Schema(
                description = "user name necessario",
                example = "bento",
                required = true
        )
        @NotBlank(message = "O primeiro nome é obrigatório")
        @Size(min = 2, max = 50, message = "Nome deve ter entre 2 e 50 caracteres")
        @Pattern(regexp = "^[A-Za-zÀ-ÿ\\s]+$", message = "Nome deve conter apenas letras")
        String username,

        @Schema(
                description = "Senha do usuário",
                example = "sdr1233",
                required = true
        )
        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
        String password,

        @Schema(
                description = "o teu role ",
                example = "ADMIN ou ESTAGIARIO",
                required = true
        )
        @NotBlank(message = "O primeiro nome é obrigatório")
        @Size(min = 2, max = 50, message = "Nome deve ter entre 2 e 50 caracteres")
        @NotEmpty String role) {
}
