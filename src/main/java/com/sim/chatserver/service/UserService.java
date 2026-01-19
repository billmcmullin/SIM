package com.sim.chatserver.service;

import java.util.List;

import org.mindrot.jbcrypt.BCrypt;

import com.sim.chatserver.model.UserAccount;
import com.sim.chatserver.startup.AppDataSourceHolder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

@ApplicationScoped
public class UserService {

    @Inject
    AppDataSourceHolder holder;

    private EntityManagerFactory emf() {
        EntityManagerFactory emf = holder.getEmf();
        if (emf == null) {
            throw new IllegalStateException("Database not configured. Please configure DB via admin UI.");
        }
        return emf;
    }

    public void ensureAdminExists() {
        EntityManager em = emf().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TypedQuery<UserAccount> q = em.createQuery(
                    "SELECT u FROM UserAccount u WHERE u.username = :u", UserAccount.class);
            q.setParameter("u", "admin");
            List<UserAccount> res = q.getResultList();
            if (res.isEmpty()) {
                UserAccount admin = new UserAccount();
                admin.setUsername("admin");
                admin.setPasswordHash(BCrypt.hashpw("admin", BCrypt.gensalt()));
                admin.setRole("ADMIN");
                admin.setFullName("Administrator");
                em.persist(admin);
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public UserAccount findByUsername(String username) {
        EntityManager em = emf().createEntityManager();
        try {
            TypedQuery<UserAccount> q = em.createQuery(
                    "SELECT u FROM UserAccount u WHERE u.username = :u", UserAccount.class);
            q.setParameter("u", username);
            List<UserAccount> res = q.getResultList();
            return res.isEmpty() ? null : res.get(0);
        } finally {
            em.close();
        }
    }

    public UserAccount createUser(String username, String password, String role) {
        EntityManager em = emf().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            UserAccount u = new UserAccount();
            u.setUsername(username);
            u.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
            u.setRole(role);
            em.persist(u);
            tx.commit();
            return u;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
