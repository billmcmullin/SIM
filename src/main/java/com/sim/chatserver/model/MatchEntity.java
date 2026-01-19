package com.sim.chatserver.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "match_entity")
public class MatchEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "chat_id")
    private String chatId;
    @Column(name = "term_id")
    private Long termId;
    @Column(name = "matched_text", columnDefinition = "text")
    private String matchedText;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = OffsetDateTime.now(); }

    // getters/setters...
}
