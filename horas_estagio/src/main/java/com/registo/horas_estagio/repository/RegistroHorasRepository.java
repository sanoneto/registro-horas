package com.registo.horas_estagio.repository;

import com.registo.horas_estagio.models.RegisterHoras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RegistroHorasRepository extends JpaRepository<RegisterHoras, UUID> {

    @Query("SELECT r FROM RegisterHoras r WHERE r.estagiario = :estagiario")
    List<RegisterHoras> findByEstagiario(@Param("estagiario") String estagiario);

}
