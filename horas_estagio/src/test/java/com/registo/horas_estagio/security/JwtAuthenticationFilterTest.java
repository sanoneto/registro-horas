// language: java
package com.registo.horas_estagio.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.registo.horas_estagio.dto.response.ErrorResponse;
import com.registo.horas_estagio.service.impl.CustomUserDetailsService;
import jakarta.servlet.WriteListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/*
  Teste unitário para JwtAuthenticationFilter.
  Cenários cobertos:
  - sem header Authorization -> delega para filterChain (doFilter chamado uma vez)
  - token inválido (JwtException) -> retorna 401 com corpo JSON de ErrorResponse
*/
@DisplayName("JwtAuthenticationFilter - Unit tests")
class JwtAuthenticationFilterTest {

    @Captor
    ArgumentCaptor<Integer> statusCaptor = ArgumentCaptor.forClass(Integer.class);

    @AfterEach
    void tearDown() {
        // limpa contexto de autenticação caso algum teste altere
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Quando não houver header Authorization deve delegar para filterChain")
    void shouldDelegateWhenNoAuthorizationHeader() throws Exception {
        // Mocks
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        JwtTokenUtil jwtTokenUtil = mock(JwtTokenUtil.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);

        when(request.getHeader("Authorization")).thenReturn(null);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenUtil, userDetailsService);

        // Execução
        filter.doFilterInternal(request, response, filterChain);

        // Verificações
        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoInteractions(jwtTokenUtil, userDetailsService);
    }

    @Test
    @DisplayName("Quando token inválido deve retornar 401 com ErrorResponse JSON")
    void shouldReturn401WhenTokenInvalid() throws Exception {
        // Mocks
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);
        JwtTokenUtil jwtTokenUtil = mock(JwtTokenUtil.class);
        CustomUserDetailsService userDetailsService = mock(CustomUserDetailsService.class);

        // Simula header com Bearer token
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token.here");

        // Configura JwtTokenUtil para lançar uma exceção ao validar
        when(jwtTokenUtil.getUsernameFromToken(anyString())).thenThrow(new io.jsonwebtoken.JwtException("invalid"));
        // ou quando(jwtTokenUtil.validateToken(anyString())).thenReturn(false); dependendo da implementação

        // Captura saída do response
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ServletOutputStream sos = new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener listener) {

            }

            @Override
            public void write(int b) throws IOException {
                baos.write(b);
            }
        };
        when(response.getOutputStream()).thenReturn(sos);

        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenUtil, userDetailsService);

        // Execução
        filter.doFilterInternal(request, response, filterChain);

        // Verifica que não delegou para a cadeia (já que escreveu resposta)
        verify(filterChain, never()).doFilter(request, response);

        // Verifica status 401 e content type
        verify(response).setStatus(401);
        verify(response).setContentType("application/json");

        // Verifica corpo JSON (ErrorResponse)
        String json = baos.toString("UTF-8");
        ObjectMapper mapper = new ObjectMapper();
        ErrorResponse err = mapper.readValue(json, ErrorResponse.class);

        assertThat(err).isNotNull();
        assertThat(err.message()).containsIgnoringCase("token");
        assertThat(err.status()).isEqualTo(401);
    }
}