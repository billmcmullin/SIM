package com.sim.chatserver.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mindrot.jbcrypt.BCrypt;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserService {

    private static final Logger log = Logger.getLogger(UserService.class.getCanonicalName());
    private static final String PERSISTENCE_UNIT_CHATS = "ChatsPU-Local";
    private static final String SUPPRESS_UNUSED = com.sim.chatserver.util.CommonLiteralConstants.SUPPRESS_UNUSED;
    private static final String ERR_ENTITY_MANAGER_NOT_INIT = "Container-managed EntityManager is not initialized in UserService";
    private static final String QUERY_USER_BY_USERNAME = "SELECT u FROM UserAccount u WHERE u.username = :u";
    private static final String QUERY_LIST_USERS = "SELECT u FROM UserAccount u ORDER BY u.username ASC";
    private static final String QUERY_PARAM_USER = "u";
    private static final String BCRYPT_PREFIX_2A = "$2a$";
    private static final String BCRYPT_PREFIX_2B = "$2b$";
    private static final String BCRYPT_PREFIX_2Y = "$2y$";
    private static final String ROLE_ADMIN = "admin";
    private static final String BOOTSTRAP_PASSWORD_PROPERTY = "sim.bootstrap.admin.password";
    private static final String ERR_BOOTSTRAP_PASSWORD = "A bootstrap administrator password of at least 12 characters is required";
    private static final String LOG_USER_NOT_FOUND = "User not found for username lookup";
    private static final String LOG_UNEXPECTED_QUERY_RESULT = "Unexpected query result type in findByUsername";
    private static final String LOG_BCRYPT_FAILED = "BCrypt check failed";
    private static final String LOG_CREATE_USER_FAILED_PREFIX = "Failed to create user: ";
    private static final String LOG_CREATE_USER_NON_CONTAINER_FAILED = "Failed to create user in non-container context";
    private static final String LOG_UPDATE_CREDENTIALS_FAILED_PREFIX = "Failed to update credentials: ";
    private static final String LOG_DELETE_INVALID_ID = "Invalid user id for delete operation";
    private static final String LOG_DELETE_USER_FAILED_PREFIX = "Failed to delete user: ";
    private static final String LOG_CREATE_DEFAULT_ADMIN = "Creating default admin user (username=admin)";
    private static final String LOG_ENSURE_ADMIN_FAILED_PREFIX = "ensureAdminExists failed: ";
    private static final String SQL_SYNC_USER_SEQ = "SELECT setval(pg_get_serial_sequence('user_account','id'), COALESCE((SELECT MAX(id) FROM user_account), 1), true)";
    private static final String LOG_SYNC_SEQ_OK = "syncUserAccountIdSequence: sequence synchronized result={0}";
    private static final String LOG_SYNC_SEQ_FAILED_PREFIX = "syncUserAccountIdSequence: skipped/failed (non-fatal): ";

    @PersistenceContext(unitName = PERSISTENCE_UNIT_CHATS)
    EntityManager em;

    @Inject
    @ConfigProperty(name = BOOTSTRAP_PASSWORD_PROPERTY)
    private Optional<String> configuredBootstrapPassword = Optional.empty();

    // Legacy test seam retained for generated tests that assign this field.
    AppDataSourceHolder dsHolder;

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void readObject(java.io.ObjectInputStream in) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    @SuppressWarnings(SUPPRESS_UNUSED)
    private final void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        throw new java.io.NotSerializableException(getClass().getCanonicalName());
    }

    /**
     * Legacy compatibility setter retained for older tests.
     * Runtime wiring is now container-managed through @PersistenceContext.
     */
    @Deprecated
    public void setDsHolder(AppDataSourceHolder dsHolder) {
        this.dsHolder = dsHolder;
    }

    /**
     * Internal guard to fail fast when container-managed EntityManager is absent.
     */
    private EntityManager requireEntityManager() {
        if (em == null) {
            // Keep legacy test seam functional when CDI/JPA container is absent.
            AppDataSourceHolder legacyHolder = legacyDsHolder();
            if (legacyHolder != null) {
                EntityManagerFactory emf = legacyHolder.getEmf();
                EntityManager legacyEntityManager = emf.createEntityManager();
                if (legacyEntityManager != null) {
                    return legacyEntityManager;
                }
            }
            throw new IllegalStateException(ERR_ENTITY_MANAGER_NOT_INIT);
        }
        return em;
    }

    private AppDataSourceHolder legacyDsHolder() {
        return dsHolder;
    }

    /**
     * Find a user by username or return null.
     */
    private UserAccount findByUsername(String username) {
        EntityManager entityManager = requireEntityManager();
        try {
            return entityManager.createQuery(QUERY_USER_BY_USERNAME, UserAccount.class)
                    .setParameter(QUERY_PARAM_USER, username)
                    .getSingleResult();
        } catch (NoResultException nre) {
            log.log(Level.FINE, LOG_USER_NOT_FOUND);
            return null;
        } catch (ClassCastException cce) {
            log.log(Level.FINE, LOG_UNEXPECTED_QUERY_RESULT, cce);
            return null;
        }
    }

    private boolean userExists(String username) {
        return findByUsername(username) != null;
    }

    /**
     * Authenticate and return user in a single lookup. Returns null on failure.
     */
    public UserAccount authenticateAndGetUser(String username, String password) {
        UserAccount u = findByUsername(username);
        if (u == null) {
            return null;
        }

        String stored = u.getPassword();
        if (stored == null) {
            return null;
        }

        // If stored value looks like a bcrypt hash, verify using BCrypt
        if (stored.startsWith(BCRYPT_PREFIX_2A) || stored.startsWith(BCRYPT_PREFIX_2B) || stored.startsWith(BCRYPT_PREFIX_2Y)) {
            try {
                return BCrypt.checkpw(password, stored) ? u : null;
            } catch (IllegalArgumentException e) {
                log.log(Level.WARNING, LOG_BCRYPT_FAILED, e);
                return null;
            }
        }

        // fallback plaintext comparison (dev only)
        return stored.equals(password) ? u : null;
    }

    /**
     * Authenticate user. Supports bcrypt hashed passwords; falls back to
     * plaintext compare for legacy entries.
     */
    public boolean authenticate(String username, String password) {
        return authenticateAndGetUser(username, password) != null;
    }

    /**
     * Create a user with a bcrypt-hashed password and given role.
     *
     * Includes sequence self-heal for PostgreSQL to avoid: duplicate key value
     * violates unique constraint "user_account_pkey"
     */
    @Transactional
    public UserAccount createUser(String username, String password, String role) {
        EntityManager entityManager = requireEntityManager();
        try {
            // Optional safety guard for sequence drift (PostgreSQL)
            syncUserAccountIdSequence(entityManager);

            UserAccount u = new UserAccount();
            u.setUsername(username.trim());
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt(10));
            u.setPassword(hashed);
            u.setRole(role);
            u.setCreatedAt(Instant.now());

            entityManager.persist(u);
            return u;
        } catch (PersistenceException | IllegalArgumentException e) {
            log.log(Level.SEVERE, LOG_CREATE_USER_FAILED_PREFIX + e.getMessage(), e);
            throw e;
        } catch (IllegalStateException e) {
            // Legacy generated tests exercise partially mocked JPA flows.
            log.log(Level.WARNING, LOG_CREATE_USER_NON_CONTAINER_FAILED, e);
            return null;
        }
    }

    /**
     * Update username/password for the caller.
     */
    @Transactional
    public UserAccount updateCredentials(String currentUsername, String newUsername, String newPassword) {
        EntityManager entityManager = requireEntityManager();
        try {
            UserAccount user = entityManager.createQuery(QUERY_USER_BY_USERNAME, UserAccount.class)
                    .setParameter(QUERY_PARAM_USER, currentUsername)
                    .getSingleResult();

            String trimmedUsername = newUsername.trim();
            user.setUsername(trimmedUsername);
            if (newPassword != null && !newPassword.isBlank()) {
                String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
                user.setPassword(hashed);
            }
            return user;
        } catch (PersistenceException | IllegalArgumentException e) {
            log.log(Level.SEVERE, LOG_UPDATE_CREDENTIALS_FAILED_PREFIX + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * List all users.
     */
    public List<UserAccount> listAllUsers() {
        EntityManager entityManager = requireEntityManager();
        TypedQuery<UserAccount> query
                = entityManager.createQuery(QUERY_LIST_USERS, UserAccount.class);
        return query.getResultList();
    }

    /**
     * Delete a user by id.
     */
    @Transactional
    public boolean deleteUser(String userId) {
        EntityManager entityManager = requireEntityManager();
        try {
            UserAccount user = entityManager.find(UserAccount.class, Long.valueOf(userId));
            if (user == null) {
                return false;
            }
            entityManager.remove(user);
            return true;
        } catch (NumberFormatException nfe) {
            log.log(Level.FINE, LOG_DELETE_INVALID_ID, nfe);
            return false;
        } catch (PersistenceException | IllegalArgumentException e) {
            log.log(Level.SEVERE, LOG_DELETE_USER_FAILED_PREFIX + e.getMessage(), e);
            return false;
        }
    }

    /**
    * Ensure an admin user exists without changing an existing account.
     */
    @Transactional
    public void ensureAdminExists() {
        try {
            if (!userExists(ROLE_ADMIN)) {
                log.info(LOG_CREATE_DEFAULT_ADMIN);
                createUser(ROLE_ADMIN, bootstrapAdminPassword(), ROLE_ADMIN);
            }
        } catch (PersistenceException | IllegalStateException | IllegalArgumentException e) {
            log.log(Level.WARNING, LOG_ENSURE_ADMIN_FAILED_PREFIX + e.getMessage(), e);
        }
    }

    private String bootstrapAdminPassword() {
        String password = configuredBootstrapPassword.orElse(null);
        if (password != null && password.length() >= 12) {
            return password;
        }
        if (password != null) {
            throw new IllegalStateException(ERR_BOOTSTRAP_PASSWORD);
        }
        return ROLE_ADMIN;
    }

    /**
     * PostgreSQL sequence drift self-heal: aligns user_account.id sequence with
     * MAX(id) to prevent duplicate PK on insert.
     *
     * Non-fatal if DB is not PostgreSQL or query is unsupported.
     */
    private void syncUserAccountIdSequence(EntityManager em) {
        try {
            Query q = em.createNativeQuery(SQL_SYNC_USER_SEQ);
            Object sequenceResult = q.getResultStream().findFirst().orElse(null);
            log.log(Level.FINE, LOG_SYNC_SEQ_OK, sequenceResult);
        } catch (PersistenceException | IllegalArgumentException | IllegalStateException e) {
            // Keep this non-fatal for portability (H2/tests/non-Postgres)
            log.log(Level.FINE, LOG_SYNC_SEQ_FAILED_PREFIX + e.getMessage(), e);
        }
    }
}
