package com.registo.horas_estagio.models;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "register_horas")
public class RegisterHoras {

    // ID interno (performance)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID público (segurança)
    @Column(unique = true, nullable = false, updatable = false)
    private UUID publicId;

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
    @JsonBackReference
    @ToString.Exclude  // ⭐ SOLUÇÃO: Exclui do toString() para evitar ciclo
    private Usuario usuario;

    @PrePersist
    @PreUpdate
    private void normalizeUsername() {
        if (this.estagiario != null && !this.estagiario.equals(this.estagiario.toLowerCase().trim())) {
            this.estagiario = this.estagiario.trim().toLowerCase();
        }
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }

}
