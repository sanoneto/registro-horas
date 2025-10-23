package com.registo.horas_estagio.controller;


import com.registo.horas_estagio.dto.request.LoginRequest;
import com.registo.horas_estagio.dto.request.UserCredentialsRequest;

import com.registo.horas_estagio.dto.response.LoginResponse;
import com.registo.horas_estagio.mapper.RequestMapper;
import com.registo.horas_estagio.models.JwtToken;
import com.registo.horas_estagio.models.Usuario;
import com.registo.horas_estagio.security.JwtTokenUtil;
import com.registo.horas_estagio.service.JwtTokenService;
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

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UsuarioService usuarioService;
    private final RequestMapper requestMapper;
    private final JwtTokenService jwtTokenService;

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

        String username = authentication.getName();
       // Tenta obter um token reutilizável (não revogado e não expirado)
               Optional<JwtToken> latestTokenOpt = jwtTokenService.getReusableTokenForUser(username);
               if (latestTokenOpt.isPresent()) {
                   JwtToken latestToken = latestTokenOpt.get();
                   log.info("Reutilizando token existente para usuário {}", username);
                   return ResponseEntity.ok().body(new LoginResponse("Login realizado com sucesso", latestToken.getToken()));
                }

        // Gera novo token e salva
        String token = jwtTokenUtil.generateToken(username);
        java.time.Instant issuedAt = java.time.Instant.now();
        java.time.Instant expiresAt = issuedAt.plusMillis(jwtTokenUtil.getExpirationMillis());
        jwtTokenService.saveToken(token, username, issuedAt, expiresAt);

        log.info("Login realizado com sucesso para: {} com o token {}", username, token);
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

        // Gera token automaticamente após registro (autologin)
        String token = jwtTokenUtil.generateToken(usuario.getUsername());

        java.time.Instant issuedAt = java.time.Instant.now();
        java.time.Instant expiresAt = issuedAt.plusMillis(jwtTokenUtil.getExpirationMillis());

        jwtTokenService.saveToken(token, usuario.getUsername(), issuedAt, expiresAt);

        // Retorna token no corpo usando o mesmo DTO LoginResponse
        return ResponseEntity.ok(new LoginResponse(usuario.getUsername() + " registrado com sucesso", token));
    }

}
