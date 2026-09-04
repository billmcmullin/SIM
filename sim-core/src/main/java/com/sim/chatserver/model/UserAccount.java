package com.sim.chatserver.model;

import java.time.Instant;

import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "user_account")
@Access(AccessType.FIELD)
public class UserAccount { 

    @SuppressWarnings("unused")
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

    @SuppressWarnings("unused")
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getName());
    }

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

    final void assignId(Long id) {
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

    private String emailInternal() {
        return email;
    }

    final String emailValue() {
        return emailInternal();
    }

    final void assignEmail(String email) {
        this.email = email;
    }

    private Instant createdAtInternal() {
        return createdAt;
    }

    final Instant createdAtValue() {
        return createdAtInternal();
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

    private String fullNameInternal() {
        return fullName != null ? fullName : username;
    }

    final String fullNameValue() {
        return fullNameInternal();
    }

    final void assignFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Compatibility method: code expects getPasswordHash() Return the stored
     * password field (replace with hashed password field later).
     */
    public String getPasswordHash() {
        return this.password;
    }

    final void assignPasswordHash(String hash) {
        this.password = hash;
    }

}
