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

import com.sim.chatserver.util.CommonLiteralConstants;

@Entity
@Table(name = UserAccount.TABLE_USER_ACCOUNT)
@Access(AccessType.FIELD)
public class UserAccount {

    public static final String TABLE_USER_ACCOUNT = "user_account";
    private static final String COLUMN_CREATED_AT = CommonLiteralConstants.SQL_COL_CREATED_AT;
    private static final String COLUMN_ROLE = CommonLiteralConstants.ROLE;
    private static final String COLUMN_FULL_NAME = "full_name";
    private static final String DEFAULT_ROLE_USER = CommonLiteralConstants.VALUE_USER;


    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;

    private String email;

    @Column(name = COLUMN_CREATED_AT)
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
    @Column(name = COLUMN_ROLE)
    private String role;

    @Column(name = COLUMN_FULL_NAME)
    private String fullName;

// add these getters/setters and compatibility methods
    public String getRole() {
        return role == null ? DEFAULT_ROLE_USER : role;
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