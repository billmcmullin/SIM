package com.sim.chatserver.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "chat")
public class Chat {
    @Id
    @Column(name="id")
    private String id;

    @Column(name="embed_uuid")
    private String embedUuid;

    @Column(name="created_at")
    private OffsetDateTime createdAt;

    @Column(columnDefinition = "text")
    private String prompt;

    @Column(columnDefinition = "text")
    private String text;

    private String language;

    @Column(columnDefinition = "jsonb")
    private String metadata; // store JSON as text; convert with Jackson if needed

    // vector and indexed_tsv handled by DB/triggers

    // getters/setters...
    public String getId() { return id; }
    public void setId(String s) { id = s; }
    public String getEmbedUuid() { return embedUuid; }
    public void setEmbedUuid(String s) { embedUuid = s; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime t) { createdAt = t; }
    public String getPrompt() { return prompt; }
    public void setPrompt(String p) { prompt = p; }
    public String getText() { return text; }
    public void setText(String t) { text = t; }
    public String getLanguage() { return language; }
    public void setLanguage(String l) { language = l; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String m) { metadata = m; }
}
