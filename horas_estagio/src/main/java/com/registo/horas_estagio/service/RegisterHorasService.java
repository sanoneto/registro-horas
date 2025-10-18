package com.registo.horas_estagio.service;

import com.registo.horas_estagio.dto.request.RegisterRequest;
import com.registo.horas_estagio.dto.response.RegisterResponse;

import java.util.List;
import java.util.UUID;

public interface RegisterHorasService {

    public RegisterResponse submitHours(RegisterRequest request);

    public List<RegisterResponse> findAllRegisteredHours();

    public List<RegisterResponse> findAllRegisteredHoursUser(String name);

    public void DeleteRegisteredHoursUser(UUID uuid);

    public RegisterResponse updateRegister(UUID uuid, RegisterRequest request);
}

