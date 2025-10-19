package com.registo.horas_estagio.config;

import com.registo.horas_estagio.security.JwtTokenUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public JwtTokenUtil jwtTokenUtil() {
        return mock(JwtTokenUtil.class);
    }
}