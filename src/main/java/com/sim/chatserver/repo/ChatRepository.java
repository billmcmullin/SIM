package com.sim.chatserver.repo;

import com.sim.chatserver.model.Chat;
import com.sim.chatserver.startup.AppDataSourceHolder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;

@ApplicationScoped
public class ChatRepository {

    @Inject
    AppDataSourceHolder holder;

    public void save(Chat c) {
        EntityManagerFactory emf = holder.getEmf();
        if (emf == null) throw new IllegalStateException("DB not configured");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(c);
            tx.commit();
        } finally {
            if (tx.isActive()) tx.rollback();
            em.close();
        }
    }

    public List<Chat> list(int offset, int limit) {
        EntityManagerFactory emf = holder.getEmf();
        if (emf == null) throw new IllegalStateException("DB not configured");
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Chat c ORDER BY c.createdAt DESC", Chat.class)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
