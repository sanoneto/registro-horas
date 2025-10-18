package com.registo.horas_estagio.service;

import com.registo.horas_estagio.dto.request.RegisterRequest;
import com.registo.horas_estagio.dto.response.PageResponse;
import com.registo.horas_estagio.dto.response.RegisterResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface RegisterHorasService {

    RegisterResponse submitHours(RegisterRequest request);

    List<RegisterResponse> findAllRegisteredHours();

    // Novo método com paginação
    PageResponse<RegisterResponse> findAllRegisteredHours(Pageable pageable);

    List<RegisterResponse> findAllRegisteredHoursUser(String name);

    // Novo método com paginação por usuário
    PageResponse<RegisterResponse> findAllRegisteredHoursUser(String name, Pageable pageable);

    void DeleteRegisteredHoursUser(UUID uuid);

    RegisterResponse updateRegister(UUID uuid, RegisterRequest request);
}

