package io.pragmia.virgilio.user.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "pragmia_virgilio_users")
@Getter @Setter @NoArgsConstructor
public class VirgilioUser {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true) private String username;
    @Column(unique = true)                   private String email;
    private String passwordHash;
    private String fullName;
    private String totpSecret;
    @Column(nullable = false) private boolean enabled     = true;
    @Column(nullable = false) private boolean locked      = false;
    @Column(nullable = false) private boolean totpEnabled = false;
    @Column(nullable = false) private Instant createdAt  = Instant.now();
    @Column(nullable = false) private Instant updatedAt  = Instant.now();
    private Instant lastLoginAt;
    @Column(nullable = false) private int loginAttempts = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "pragmia_virgilio_user_roles",
               joinColumns = @JoinColumn(name = "user_id"),
               inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<VirgilioRole> roles = new HashSet<>();

    @PreUpdate public void preUpdate() { updatedAt = Instant.now(); }
}
