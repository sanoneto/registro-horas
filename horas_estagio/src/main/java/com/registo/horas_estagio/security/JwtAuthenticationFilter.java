package com.registo.horas_estagio.security;

import com.registo.horas_estagio.dto.response.ErrorResponse;
import com.registo.horas_estagio.exception.GlobalExceptionHandler;
import com.registo.horas_estagio.service.impl.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Se não houver header Authorization ou não começar com "Bearer ", pula o filtro
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            // Extrai o token (remove "Bearer " do início)
            final String jwt = authHeader.substring(7);
            final String username = jwtTokenUtil.getUsernameFromToken(jwt);

            // Se o username foi extraído e não há autenticação no contexto
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Valida o token
                if (jwtTokenUtil.validateToken(jwt)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            // segue a cadeia normalmente
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException eje) {
            // Token expirado: devolve 401 com corpo JSON
            log.warn("Token JWT expirado para request {}: {}", request.getRequestURI(), eje.getMessage());
            writeErrorResponse(response, "Token JWT expirado");
        } catch (JwtException | IllegalArgumentException e) {
            // Token mal formado / assinatura inválida / outros problemas de JWT
            log.warn("Token JWT inválido para request {}: {}", request.getRequestURI(), e.getMessage());
            writeErrorResponse(response, "Token JWT inválido");
        } catch (Exception e) {
            // Erro inesperado: log e devolver 401 por segurança
            log.error("Erro inesperado ao validar token JWT: {}", e.getMessage(), e);
            writeErrorResponse(response, "Erro ao processar autenticação");
        }
    }

    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        ErrorResponse err = new ErrorResponse(message, HttpServletResponse.SC_UNAUTHORIZED);
        byte[] json = objectMapper.writeValueAsString(err).getBytes(StandardCharsets.UTF_8);
        response.getOutputStream().write(json);
    }
}