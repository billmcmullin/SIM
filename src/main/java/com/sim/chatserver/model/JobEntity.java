package com.sim.chatserver.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_entity")
public class JobEntity {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;
    private String type;
    private String status;
    @Column(columnDefinition = "jsonb")
    private String params;
    @Column(columnDefinition = "jsonb")
    private String progress;
    @Column(name = "started_at")
    private OffsetDateTime startedAt;
    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    @PrePersist
    public void prePersist() { if (id == null) id = UUID.randomUUID(); }
    // getters/setters...
}
