// language: java
package com.registo.horas_estagio.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OpenApiConfig - Unit tests")
class OpenApiConfigTest {

    @Test
    @DisplayName("Deve criar OpenAPI com servidores e security scheme configurados")
    void shouldCreateOpenApiWithServersAndSecurityScheme() throws Exception {
        // Given
        OpenApiConfig config = new OpenApiConfig();

        // Injeta valor no campo privado serverPort (simula @Value)
        Field serverPortField = OpenApiConfig.class.getDeclaredField("serverPort");
        serverPortField.setAccessible(true);
        serverPortField.set(config, "9090");

        // When
        OpenAPI openAPI = config.customOpenAPI();

        // Then
        assertThat(openAPI).isNotNull();

        // Verifica servers
        List<Server> servers = openAPI.getServers();
        assertThat(servers).isNotNull().hasSizeGreaterThanOrEqualTo(1);
        assertThat(servers.get(0).getUrl()).isEqualTo("http://localhost:9090");
        // verifica que exista um servidor de produção também
        assertThat(servers.stream().anyMatch(s -> "https://api.producao.com".equals(s.getUrl()))).isTrue();

        // Verifica components e security scheme
        Components components = openAPI.getComponents();
        assertThat(components).isNotNull();
        SecurityScheme scheme = components.getSecuritySchemes().get("bearerAuth");
        assertThat(scheme).isNotNull();
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
    }
}