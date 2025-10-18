package com.registo.horas_estagio.mapper;


import com.registo.horas_estagio.dto.LoginRequestDTO;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-17T03:46:52+0100",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.1 (Oracle Corporation)"
)
@Component
public class LoginRequestMapperImpl implements LoginRequestMapper {

    @Override
    public LoginRequestDTO toLoginRequestDTO(LoginRequest loginRequest) {
        if ( loginRequest == null ) {
            return null;
        }

        LoginRequestDTO loginRequestDTO = new LoginRequestDTO();

        loginRequestDTO.setUsername( loginRequest.username() );
        loginRequestDTO.setPassword( loginRequest.password() );

        return loginRequestDTO;
    }
}
