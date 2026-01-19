package com.sim.chatserver.startup;

import jakarta.enterprise.context.ApplicationScoped;

import javax.sql.DataSource;
import jakarta.persistence.EntityManagerFactory;

@ApplicationScoped
public class AppDataSourceHolder {
    private volatile DataSource dataSource;
    private volatile EntityManagerFactory emf;

    public DataSource getDataSource() { return dataSource; }
    public void setDataSource(DataSource ds) { this.dataSource = ds; }

    public EntityManagerFactory getEmf() { return emf; }
    public void setEmf(EntityManagerFactory emf) { this.emf = emf; }
}
