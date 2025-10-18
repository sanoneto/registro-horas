package com.registo.horas_estagio.controller;


import com.registo.horas_estagio.dto.request.RegisterRequest;
import com.registo.horas_estagio.dto.response.RegisterResponse;
import com.registo.horas_estagio.service.RegisterHorasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/registos")
@RequiredArgsConstructor
public class RegistroHorasController {

    private final RegisterHorasService registerHorasService;

    @Operation(
            summary = "Listar todos os registos",
            description = "Retorna todos os registros de horas (apenas ADMIN)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN")
    })
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RegisterResponse>> getAllRegisterHoras() {
        return ResponseEntity.ok(registerHorasService.findAllRegisteredHours());
    }
    @Operation(
            summary = "Listar registos por usuário",
            description = "Retorna os registros de um usuário específico (ADMIN vê todos, ESTAGIARIO vê apenas os seus)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Nenhum registro encontrado")
    })
    @GetMapping("/list/{name}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ESTAGIARIO') and #name == authentication.name)")
    public ResponseEntity<List<RegisterResponse>> getAllRegisterHorasUser(@PathVariable String name) {
        return ResponseEntity.ok(registerHorasService.findAllRegisteredHoursUser(name));
    }

    @Operation(
            summary = "Criar novo registo",
            description = "Cria um novo registro de horas de estágio"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Registro criado com sucesso",
                    content = @Content(schema = @Schema(implementation = RegisterResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'ESTAGIARIO')")
    public ResponseEntity<RegisterResponse> addRegisterHoras(@RequestBody @Valid RegisterRequest registerRequest) {
       RegisterResponse registerResponse= registerHorasService.submitHours(registerRequest);
        return new ResponseEntity<>(registerResponse, HttpStatus.CREATED);
    }
    @Operation(
            summary = "Atualizar registo",
            description = "Atualiza um registro existente (ADMIN pode editar qualquer, ESTAGIARIO apenas os seus)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Registro atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PutMapping("update/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('ESTAGIARIO') and #registerRequest.estagiario() == authentication.name)")
    public ResponseEntity<RegisterResponse> updateRegister(
            @PathVariable UUID uuid,
            @RequestBody @Valid RegisterRequest registerRequest) {
        // ADMIN pode editar qualquer registro
        // ESTAGIARIO só pode editar seus próprios registros
        RegisterResponse response = registerHorasService.updateRegister(uuid, registerRequest);
        return ResponseEntity.ok(response);
    }
    @Operation(
            summary = "Apgagar registro",
            description = "Remove um registro de horas (apenas ADMIN)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Registro deletado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado - apenas ADMIN")
    })
    @DeleteMapping("delete/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRegister(@PathVariable UUID uuid) {
        registerHorasService.DeleteRegisteredHoursUser(uuid);
        return ResponseEntity.noContent().build();
    }
}
