package com.registo.horas_estagio.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "usuario", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")
})
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ID público (segurança)
    @Column(unique = true, nullable = false, updatable = false)
    private UUID publicId;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Username não pode ser vazio") // Adicionar
    private String username;

    @Column(nullable = false)
    @JsonIgnore
    @NotBlank(message = "Password não pode ser vazio") // Adicionar
    private String password;

    @Column(nullable = false)
    @NotBlank(message = "Role não pode ser vazia") // Adicionar
    private String role;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude  // SOLUÇÃO: Exclui do toString() para evitar ciclo
    private List<RegisterHoras> registros = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    @ToString.Exclude  // SOLUÇÃO: Exclui do toString() para evitar ciclo
    private List<JwtToken> jwtToken = new ArrayList<>();

    /**
            * Setter customizado para username.
            * Normaliza automaticamente para lowercase.
            * @param username o nome de usuário a ser definido
     */
    public void setUsername(String username) {
        this.username = username != null ? username.trim().toLowerCase() : null;
    }

    /**
     * Backup de normalização caso o setter não seja chamado.
     * Executado antes de INSERT ou UPDATE no banco de dados.
     */
    @PrePersist
    @PreUpdate
    private void normalizeUsername() {
        if (this.username != null && !this.username.equals(this.username.toLowerCase().trim())) {
            this.username = this.username.trim().toLowerCase();
        }
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }

}