package io.pragmia.canto.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "canto_flow_trees")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FlowTree {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 512)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String definitionJson;

    @Column(nullable = false)
    private boolean active = false;

    @Column(nullable = false)
    private int version = 1;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;
}
