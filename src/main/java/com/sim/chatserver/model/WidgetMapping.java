package com.sim.chatserver.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "widget_mapping")
public class WidgetMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="embed_uuid", unique=true, nullable=false)
    private String embedUuid;

    @Column(name="display_name")
    private String displayName;

    @Column(name="category")
    private String category;

    @Column(name="configured_by", columnDefinition = "uuid")
    private java.util.UUID configuredBy;

    @Column(name="created_at")
    private OffsetDateTime createdAt;

    @Column(name="last_sync_at")
    private OffsetDateTime lastSyncAt;

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = OffsetDateTime.now(); }

    // getters/setters...
    public Long getId() { return id; }
    public String getEmbedUuid() { return embedUuid; }
    public void setEmbedUuid(String s) { this.embedUuid = s; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String s) { this.displayName = s; }
    public String getCategory() { return category; }
    public void setCategory(String c) { this.category = c; }
    public OffsetDateTime getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(OffsetDateTime t) { this.lastSyncAt = t; }
}
