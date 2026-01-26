package com.sim.chatserver.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_account")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    private String email;

    @Column(name = "created_at")
    private Instant createdAt;

    // getters & setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    // inside class UserAccount (add fields)
    @Column(name = "role")
    private String role;

    @Column(name = "full_name")
    private String fullName;

// add these getters/setters and compatibility methods
    public String getRole() {
        return role == null ? "user" : role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName != null ? fullName : username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Compatibility method: code expects getPasswordHash() Return the stored
     * password field (replace with hashed password field later).
     */
    public String getPasswordHash() {
        return this.password;
    }

    public void setPasswordHash(String hash) {
        this.password = hash;
    }

}
