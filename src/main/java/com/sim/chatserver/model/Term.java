package com.sim.chatserver.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "term")
public class Term {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique=true)
    private String label;
    @Column(name="raw_pattern", columnDefinition = "text")
    private String rawPattern;
    @Column(name="is_regex")
    private boolean isRegex;
    @Column(name="created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = OffsetDateTime.now(); }

    // getters/setters...
    public Long getId() { return id; }
    public String getLabel() { return label; }
    public void setLabel(String l) { label = l; }
    public String getRawPattern() { return rawPattern; }
    public void setRawPattern(String p) { rawPattern = p; }
    public boolean isRegex() { return isRegex; }
    public void setRegex(boolean r) { isRegex = r; }
}
