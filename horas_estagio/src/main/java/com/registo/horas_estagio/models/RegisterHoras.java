package com.registo.horas_estagio.models;



import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;


@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "register_horas")
public class RegisterHoras {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String estagiario;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false)
    private LocalDateTime dataInicio;

    @Column(nullable = false)
    private LocalDateTime dataFim;

    @Column(nullable = false)
    private int horasTrabalhadas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonBackReference  // <-- Evita serialização circular
    private Usuario usuario;



}
