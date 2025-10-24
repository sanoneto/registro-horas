package com.registo.horas_estagio.service;

import com.registo.horas_estagio.dto.request.RegisterRequest;
import com.registo.horas_estagio.dto.response.PageResponse;
import com.registo.horas_estagio.dto.response.RegisterResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RegisterHorasService {

    RegisterResponse submitHours(RegisterRequest request);

    List<RegisterResponse> findAllRegisteredHours();


    PageResponse<RegisterResponse> findAllRegisteredHours(Pageable pageable);

    List<RegisterResponse> findAllRegisteredHoursUser(String name);

    PageResponse<RegisterResponse> findAllRegisteredHoursUser(String name, Pageable pageable);

    void DeleteRegisteredHoursUser(UUID publicId);

    RegisterResponse updateRegister(UUID publicId, RegisterRequest request);

    Map<Integer, Double> getWeeklyHoursForYear(int year, String estagiario);

    // Retorna total de horas registadas por um estagiário (todos os registos)
    double getTotalHoursForUser(String estagiario);
}

