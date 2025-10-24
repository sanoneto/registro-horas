package com.registo.horas_estagio.repository;

import com.registo.horas_estagio.models.RegisterHoras;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistroHorasRepository extends JpaRepository<RegisterHoras, Long> {

    @Query("SELECT r FROM RegisterHoras r WHERE r.estagiario = :estagiario")
    List<RegisterHoras> findByEstagiario(@Param("estagiario") String estagiario);


    Page<RegisterHoras> findByEstagiario(String name, Pageable pageable);


    Optional<RegisterHoras> findByPublicId(UUID attr0);

    // Busca registros entre duas datas (todos os utilizadores)
    List<RegisterHoras> findByDataInicioBetween(LocalDateTime start, LocalDateTime end);

    // Busca registros de um utilizador entre duas datas
    List<RegisterHoras> findByEstagiarioAndDataInicioBetween(String estagiario, LocalDateTime start, LocalDateTime end);
}
