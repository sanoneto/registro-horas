package com.registo.horas_estagio.mapper;

import com.registo.horas_estagio.dto.request.RegisterRequest;
import com.registo.horas_estagio.dto.request.UserCredentialsRequest;
import com.registo.horas_estagio.dto.response.RegisterResponse;
import com.registo.horas_estagio.models.RegisterHoras;
import com.registo.horas_estagio.models.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "publicId", source = "publicId")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    @Mapping(target = "role", source = "role")
    Usuario mapToRegisterHoras(UserCredentialsRequest userCredentialsRequest);


    @Mapping(target = "publicId", source = "publicId")
    @Mapping(target = "estagiario", source = "estagiario")
    @Mapping(target = "descricao", source = "descricao")
    @Mapping(target = "dataInicio", source = "dataInicio")
    @Mapping(target = "dataFim", source = "dataFim")
    @Mapping(target = "horasTrabalhadas", source = "horasTrabalhadas")
    RegisterHoras mapToRegisterHoras(RegisterRequest registerRequest);
    RegisterResponse mapRegisterResponse(RegisterHoras registerHoras);


    @Mapping(target = "List<RegisterHoras> ", source = "List<RegisterResponse> ")
    List<RegisterResponse>  mapToListRegisterResponse(List<RegisterHoras> registerHoras);

}
