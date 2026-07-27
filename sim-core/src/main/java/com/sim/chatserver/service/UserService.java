package com.sim.chatserver.service;

import java.time.Instant;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mindrot.jbcrypt.BCrypt;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserService {

    private static final Logger log = Logger.getLogger(UserService.class.getName());

    @Inject
    AppDataSourceHolder dsHolder;

    /**
     * Setter for non-CDI fallback wiring (e.g., local Jetty UI test runtime).
     */
    public void setDsHolder(AppDataSourceHolder dsHolder) {
        this.dsHolder = dsHolder;
    }

    /**
     * Internal guard to fail fast with clear message when datasource is not
     * wired.
     */
    private EntityManagerFactory requireEmf() {
        if (dsHolder == null) {
            throw new IllegalStateException("AppDataSourceHolder is not initialized in UserService");
        }
        EntityManagerFactory emf = dsHolder.getEmf();
        if (emf == null) {
            throw new IllegalStateException("EntityManagerFactory is null in AppDataSourceHolder");
        }
        return emf;
    }

    /**
     * Find a user by username or return null.
     */
    public UserAccount findByUsername(String username) {
        EntityManagerFactory emf = requireEmf();
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM UserAccount u WHERE u.username = :u", UserAccount.class)
                    .setParameter("u", username)
                    .getSingleResult();
        } catch (NoResultException nre) {
            log.log(Level.FINE, "User not found for username lookup");
            return null;
        } finally {
            em.close();
        }
    }

    public boolean userExists(String username) {
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
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            try {
                return BCrypt.checkpw(password, stored) ? u : null;
            } catch (IllegalArgumentException e) {
                log.log(Level.WARNING, "BCrypt check failed", e);
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
        EntityManagerFactory emf = requireEmf();
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            // Optional safety guard for sequence drift (PostgreSQL)
            syncUserAccountIdSequence(em);

            UserAccount u = new UserAccount();
            u.setUsername(username.trim());
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt(10));
            u.setPassword(hashed);
            u.setRole(role);
            u.setCreatedAt(Instant.now());

            em.persist(u);
            em.getTransaction().commit();
            return u;
        } catch (PersistenceException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "Failed to create user: " + e.getMessage(), e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Update username/password for the caller.
     */
    @Transactional
    public UserAccount updateCredentials(String currentUsername, String newUsername, String newPassword) {
        EntityManagerFactory emf = requireEmf();
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            UserAccount user = em.createQuery("SELECT u FROM UserAccount u WHERE u.username = :u", UserAccount.class)
                    .setParameter("u", currentUsername)
                    .getSingleResult();

            String trimmedUsername = newUsername.trim();
            user.setUsername(trimmedUsername);
            if (newPassword != null && !newPassword.isBlank()) {
                String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
                user.setPassword(hashed);
            }
            em.getTransaction().commit();
            return user;
        } catch (PersistenceException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "Failed to update credentials: " + e.getMessage(), e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * List all users.
     */
    public List<UserAccount> listAllUsers() {
        EntityManagerFactory emf = requireEmf();
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<UserAccount> query
                    = em.createQuery("SELECT u FROM UserAccount u ORDER BY u.username ASC", UserAccount.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    /**
     * Delete a user by id.
     */
    @Transactional
    public boolean deleteUser(String userId) {
        EntityManagerFactory emf = requireEmf();
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            UserAccount user = em.find(UserAccount.class, Long.valueOf(userId));
            if (user == null) {
                em.getTransaction().rollback();
                return false;
            }
            em.remove(user);
            em.getTransaction().commit();
            return true;
        } catch (NumberFormatException nfe) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return false;
        } catch (PersistenceException | IllegalArgumentException e) {
            log.log(Level.SEVERE, "Failed to delete user: " + e.getMessage(), e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * Ensure an admin user exists (creates admin/admin if none found).
     */
    public void ensureAdminExists() {
        try {
            if (!userExists("admin")) {
                log.info("Creating default admin user (username=admin)");
                createUser("admin", "admin", "admin");
            }
        } catch (PersistenceException | IllegalStateException | IllegalArgumentException e) {
            log.log(Level.WARNING, "ensureAdminExists failed: " + e.getMessage(), e);
        }
    }

    /**
     * PostgreSQL sequence drift self-heal: aligns user_account.id sequence with
     * MAX(id) to prevent duplicate PK on insert.
     *
     * Non-fatal if DB is not PostgreSQL or query is unsupported.
     */
    private void syncUserAccountIdSequence(EntityManager em) {
        try {
            Query q = em.createNativeQuery(
                    "SELECT setval("
                    + "pg_get_serial_sequence('user_account','id'), "
                    + "COALESCE((SELECT MAX(id) FROM user_account), 1), "
                    + "true)"
            );
            q.getResultStream().findFirst().orElse(null);
            log.fine("syncUserAccountIdSequence: sequence synchronized");
        } catch (PersistenceException | IllegalArgumentException | IllegalStateException e) {
            // Keep this non-fatal for portability (H2/tests/non-Postgres)
            log.log(Level.FINE, "syncUserAccountIdSequence: skipped/failed (non-fatal): " + e.getMessage(), e);
        }
    }
}
