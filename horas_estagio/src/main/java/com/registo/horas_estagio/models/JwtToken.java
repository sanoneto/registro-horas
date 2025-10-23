package com.registo.horas_estagio.models;

// language: java
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;
@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "jwt_token")
public class JwtToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, length = 2000)
    private String token;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId ;

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