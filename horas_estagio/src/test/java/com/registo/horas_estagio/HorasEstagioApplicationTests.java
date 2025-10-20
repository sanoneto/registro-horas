package com.registo.horas_estagio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Teste de sanity que carrega o contexto da aplicação para o profile de teste.
 * A configuração específica para testes (H2 in-memory, exclusões de auto-config, etc.)
 * deve residir em src/test/resources/application-test.yml e ser ativada pelo profile "test".
 */
@SpringBootTest
@ActiveProfiles("test")
class HorasEstagioApplicationTests {

    @Test
    @DisplayName("Deve carregar o contexto da aplicação (profile: test)")
    void shouldLoadApplicationContext() {
    }

}