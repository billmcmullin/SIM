package com.sim.chatserver.service;

import java.time.Instant;
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
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserService {

    private static final Logger log = Logger.getLogger(UserService.class.getName());

    @Inject
    AppDataSourceHolder dsHolder;

    /**
     * Find a user by username or return null.
     */
    public UserAccount findByUsername(String username) {
        EntityManagerFactory emf = dsHolder.getEmf();
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT u FROM UserAccount u WHERE u.username = :u", UserAccount.class)
                    .setParameter("u", username)
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } finally {
            em.close();
        }
    }

    public boolean userExists(String username) {
        return findByUsername(username) != null;
    }

    /**
     * Authenticate user. Supports bcrypt hashed passwords; falls back to
     * plaintext compare for legacy entries.
     */
    public boolean authenticate(String username, String password) {
        UserAccount u = findByUsername(username);
        if (u == null) {
            return false;
        }
        String stored = u.getPassword(); // existing field
        if (stored == null) {
            return false;
        }

        // If stored value looks like a bcrypt hash, verify using BCrypt
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            try {
                return BCrypt.checkpw(password, stored);
            } catch (Exception e) {
                log.log(Level.WARNING, "BCrypt check failed", e);
                return false;
            }
        }

        // fallback plaintext comparison (dev only)
        return stored.equals(password);
    }

    /**
     * Create a user with a bcrypt-hashed password and given role.
     */
    @Transactional
    public UserAccount createUser(String username, String password, String role) {
        EntityManagerFactory emf = dsHolder.getEmf();
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            UserAccount u = new UserAccount();
            u.setUsername(username);
            // Hash password for storage
            String hashed = BCrypt.hashpw(password, BCrypt.gensalt(10));
            u.setPassword(hashed);
            u.setRole(role);
            u.setCreatedAt(Instant.now());
            em.persist(u);
            em.getTransaction().commit();
            return u;
        } catch (Exception e) {
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
     * Ensure an admin user exists (creates admin/admin if none found).
     */
    public void ensureAdminExists() {
        try {
            if (!userExists("admin")) {
                log.info("Creating default admin user (username=admin)");
                createUser("admin", "admin", "admin");
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "ensureAdminExists failed: " + e.getMessage(), e);
        }
    }
}
