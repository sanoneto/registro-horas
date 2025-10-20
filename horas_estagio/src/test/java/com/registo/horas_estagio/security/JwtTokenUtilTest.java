package com.registo.horas_estagio.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes do JwtTokenUtil")
class JwtTokenUtilTest {

    private JwtTokenUtil jwtTokenUtil;
    private String testSecret;
    private Long testExpiration;

    @BeforeEach
    void setUp() {
        jwtTokenUtil = new JwtTokenUtil();

        // Secret deve ter pelo menos 256 bits (32 caracteres) para HS256
        testSecret = "test-secret-key-for-testing-purposes-must-be-long-enough";
        testExpiration = 86400000L; // 24 horas em milissegundos

        // Injeta os valores usando ReflectionTestUtils
        ReflectionTestUtils.setField(jwtTokenUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtTokenUtil, "expiration", testExpiration);
    }

    @Test
    @DisplayName("Deve gerar token JWT válido")
    void shouldGenerateValidJwtToken() {
        // Given
        String username = "neto";

        // When
        String token = jwtTokenUtil.generateToken(username);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT tem 3 partes separadas por ponto
    }

    @Test
    @DisplayName("Deve gerar tokens únicos devido aos timestamps")
    void shouldGenerateDifferentTokensForSameUser() throws InterruptedException {
        // Given
        String username = "neto";

        // When - Gera primeiro token
        String token1 = jwtTokenUtil.generateToken(username);
        Claims claims1 = parseTokenClaims(token1);
        Date issuedAt1 = claims1.getIssuedAt();

        // Espera até o timestamp mudar
        String token2;
        Claims claims2;
        Date issuedAt2;
        int attempts = 0;
        int maxAttempts = 100;

        do {
            Thread.sleep(10); // Espera 10ms
            token2 = jwtTokenUtil.generateToken(username);
            claims2 = parseTokenClaims(token2);
            issuedAt2 = claims2.getIssuedAt();
            attempts++;
        } while (issuedAt1.equals(issuedAt2) && attempts < maxAttempts);

        // Then - Garantir que conseguimos timestamps diferentes
        assertThat(attempts).isLessThan(maxAttempts)
                .withFailMessage("Não foi possível gerar tokens com timestamps diferentes após %d tentativas", maxAttempts);

        // Tokens devem ser diferentes
        assertThat(token1).isNotEqualTo(token2);

        // Verifica que ambos são válidos
        assertThat(jwtTokenUtil.validateToken(token1)).isTrue();
        assertThat(jwtTokenUtil.validateToken(token2)).isTrue();

        // Verifica que ambos têm o mesmo username
        assertThat(jwtTokenUtil.getUsernameFromToken(token1)).isEqualTo(username);
        assertThat(jwtTokenUtil.getUsernameFromToken(token2)).isEqualTo(username);

        // Verifica que os timestamps de emissão são diferentes
        assertThat(claims1.getIssuedAt()).isBefore(claims2.getIssuedAt());
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes para usuários diferentes")
    void shouldGenerateDifferentTokensForDifferentUsers() {
        // Given
        String username1 = "neto";
        String username2 = "admin";

        // When
        String token1 = jwtTokenUtil.generateToken(username1);
        String token2 = jwtTokenUtil.generateToken(username2);

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("Deve extrair username do token corretamente")
    void shouldExtractUsernameFromToken() {
        // Given
        String username = "neto";
        String token = jwtTokenUtil.generateToken(username);

        // When
        String extractedUsername = jwtTokenUtil.getUsernameFromToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("Deve extrair username correto de diferentes tokens")
    void shouldExtractCorrectUsernameFromDifferentTokens() {
        // Given
        String username1 = "neto";
        String username2 = "admin";
        String token1 = jwtTokenUtil.generateToken(username1);
        String token2 = jwtTokenUtil.generateToken(username2);

        // When
        String extractedUsername1 = jwtTokenUtil.getUsernameFromToken(token1);
        String extractedUsername2 = jwtTokenUtil.getUsernameFromToken(token2);

        // Then
        assertThat(extractedUsername1).isEqualTo(username1);
        assertThat(extractedUsername2).isEqualTo(username2);
    }

    @Test
    @DisplayName("Deve validar token válido com sucesso")
    void shouldValidateValidToken() {
        // Given
        String username = "neto";
        String token = jwtTokenUtil.generateToken(username);

        // When
        boolean isValid = jwtTokenUtil.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar token inválido")
    void shouldRejectInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid = jwtTokenUtil.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar token vazio")
    void shouldRejectEmptyToken() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtTokenUtil.validateToken(emptyToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar token null")
    void shouldRejectNullToken() {
        // Given
        String nullToken = null;

        // When
        boolean isValid = jwtTokenUtil.validateToken(nullToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar token com assinatura inválida")
    void shouldRejectTokenWithInvalidSignature() {
        // Given
        String username = "neto";
        String token = jwtTokenUtil.generateToken(username);

        // Modifica o token para invalidar a assinatura
        String[] parts = token.split("\\.");
        String invalidToken = parts[0] + "." + parts[1] + ".invalid-signature";

        // When
        boolean isValid = jwtTokenUtil.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar token assinado com chave diferente")
    void shouldRejectTokenSignedWithDifferentKey() {
        // Given
        String username = "neto";
        String differentSecret = "different-secret-key-that-is-long-enough-for-testing";
        SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));

        String tokenWithDifferentKey = Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + testExpiration))
                .signWith(differentKey)
                .compact();

        // When
        boolean isValid = jwtTokenUtil.validateToken(tokenWithDifferentKey);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar token expirado")
    void shouldRejectExpiredToken() {
        // Given
        String username = "neto";
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));

        // Cria token que já está expirado
        String expiredToken = Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis() - 2000))
                .expiration(new Date(System.currentTimeMillis() - 1000)) // Expirou 1 segundo atrás
                .signWith(key)
                .compact();

        // When
        boolean isValid = jwtTokenUtil.validateToken(expiredToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve lançar exceção ao extrair username de token inválido")
    void shouldThrowExceptionWhenExtractingUsernameFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThatThrownBy(() -> jwtTokenUtil.getUsernameFromToken(invalidToken))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção ao extrair username de token com assinatura inválida")
    void shouldThrowExceptionWhenExtractingUsernameFromTokenWithInvalidSignature() {
        // Given
        String username = "neto";
        String token = jwtTokenUtil.generateToken(username);
        String[] parts = token.split("\\.");
        String invalidToken = parts[0] + "." + parts[1] + ".invalid-signature";

        // When & Then
        assertThatThrownBy(() -> jwtTokenUtil.getUsernameFromToken(invalidToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção ao extrair username de token expirado")
    void shouldThrowExceptionWhenExtractingUsernameFromExpiredToken() {
        // Given
        String username = "neto";
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));

        String expiredToken = Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis() - 2000))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(key)
                .compact();

        // When & Then
        assertThatThrownBy(() -> jwtTokenUtil.getUsernameFromToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Deve gerar token com data de emissão e expiração corretas")
    void shouldGenerateTokenWithCorrectIssuedAndExpirationDates() {
        // Given
        String username = "neto";

        // When
        String token = jwtTokenUtil.generateToken(username);

        // Then
        Claims claims = parseTokenClaims(token);
        Date issuedAt = claims.getIssuedAt();
        Date expiration = claims.getExpiration();

        // Valida que a diferença entre expiração e emissão é exatamente testExpiration
        long actualDuration = expiration.getTime() - issuedAt.getTime();
        assertThat(actualDuration).isEqualTo(testExpiration);

        // Valida que o token foi emitido recentemente (nos últimos 2 segundos)
        long now = System.currentTimeMillis();
        long timeSinceIssued = now - issuedAt.getTime();
        assertThat(timeSinceIssued).isLessThan(2000L);

        // Valida o subject
        assertThat(claims.getSubject()).isEqualTo(username);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Método auxiliar para fazer parse dos claims do token
     */
    private Claims parseTokenClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Test
    @DisplayName("Deve preservar username com caracteres especiais")
    void shouldPreserveUsernameWithSpecialCharacters() {
        // Given
        String[] usernames = {"user@example.com", "user.name", "user_name", "user-name"};

        for (String username : usernames) {
            // When
            String token = jwtTokenUtil.generateToken(username);
            String extractedUsername = jwtTokenUtil.getUsernameFromToken(token);

            // Then
            assertThat(extractedUsername).isEqualTo(username);
        }
    }

    @Test
    @DisplayName("Deve preservar username case-sensitive")
    void shouldPreserveUsernameCaseSensitive() {
        // Given
        String username1 = "NETO";
        String username2 = "neto";
        String username3 = "Neto";

        // When
        String token1 = jwtTokenUtil.generateToken(username1);
        String token2 = jwtTokenUtil.generateToken(username2);
        String token3 = jwtTokenUtil.generateToken(username3);

        String extracted1 = jwtTokenUtil.getUsernameFromToken(token1);
        String extracted2 = jwtTokenUtil.getUsernameFromToken(token2);
        String extracted3 = jwtTokenUtil.getUsernameFromToken(token3);

        // Then
        assertThat(extracted1).isEqualTo(username1);
        assertThat(extracted2).isEqualTo(username2);
        assertThat(extracted3).isEqualTo(username3);
    }

    @Test
    @DisplayName("Deve validar múltiplos tokens independentemente")
    void shouldValidateMultipleTokensIndependently() {
        // Given
        String token1 = jwtTokenUtil.generateToken("neto");
        String token2 = jwtTokenUtil.generateToken("admin");
        String invalidToken = "invalid.token.here";

        // When
        boolean isValid1 = jwtTokenUtil.validateToken(token1);
        boolean isValid2 = jwtTokenUtil.validateToken(token2);
        boolean isInvalid = jwtTokenUtil.validateToken(invalidToken);

        // Then
        assertThat(isValid1).isTrue();
        assertThat(isValid2).isTrue();
        assertThat(isInvalid).isFalse();
    }

    @Test
    @DisplayName("Deve gerar token válido com username contendo espaços")
    void shouldGenerateValidTokenWithUsernameContainingSpaces() {
        // Given
        String username = "user name with spaces";

        // When
        String token = jwtTokenUtil.generateToken(username);
        String extractedUsername = jwtTokenUtil.getUsernameFromToken(token);

        // Then
        assertThat(token).isNotNull();
        assertThat(extractedUsername).isEqualTo(username);
        assertThat(jwtTokenUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Deve funcionar com username muito longo")
    void shouldWorkWithVeryLongUsername() {
        // Given
        String longUsername = "a".repeat(500); // Username com 500 caracteres

        // When
        String token = jwtTokenUtil.generateToken(longUsername);
        String extractedUsername = jwtTokenUtil.getUsernameFromToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo(longUsername);
        assertThat(jwtTokenUtil.validateToken(token)).isTrue();
    }
}