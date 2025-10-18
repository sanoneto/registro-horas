package com.registo.horas_estagio.controller;


import com.registo.horas_estagio.dto.request.LoginRequest;
import com.registo.horas_estagio.dto.request.UserCredentialsRequest;

import com.registo.horas_estagio.dto.response.LoginResponse;
import com.registo.horas_estagio.mapper.RequestMapper;
import com.registo.horas_estagio.models.Usuario;
import com.registo.horas_estagio.security.JwtTokenUtil;
import com.registo.horas_estagio.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UsuarioService usuarioService;
    private final RequestMapper requestMapper;

    @Operation(
            summary = "Realizar login na aplicaçao",
            description = "Autentica o usuário e retorna um token JWT válido por 24 horas"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida")
    })

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        log.info("Tentativa de login: {}", loginRequest.username());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password())
        );

        String token = jwtTokenUtil.generateToken(authentication.getName());
        log.info("Login realizado com sucesso para: {}", loginRequest.username());
        log.info("Token gerado: {}", token);
        return ResponseEntity.ok().body(new LoginResponse("Login realizado com sucesso", token));
    }

    /**
     * Endpoint para registrar um novo Username.
     */
    @Operation(
            summary = "Registrar novo usuário",
            description = "Cria um novo usuário no sistema"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Username já existe ou dados inválidos")
    })
    @PostMapping("/registar")
    public ResponseEntity<?> registrarUsuario(@RequestBody UserCredentialsRequest userCredentialsRequest) {
        if (usuarioService.existeUsuario(userCredentialsRequest.username())) {
            return ResponseEntity.badRequest().body("Username já existe!");
        }
        Usuario usuario = requestMapper.mapToRegisterHoras(userCredentialsRequest);
        usuarioService.registrarUsuario(usuario);
        return ResponseEntity.ok("Username registrado com sucesso!");
    }

}
