package com.example.study.configurer;

import org.hibernate.SessionFactory;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Component
public class JpaBatchConfigurer extends DefaultBatchConfigurer {

    private DataSource dataSource;
    private EntityManagerFactory entityManagerFactory;
    private PlatformTransactionManager transactionManager;

    public JpaBatchConfigurer(DataSource dataSource, EntityManagerFactory entityManagerFactory) {
        super(dataSource);
        this.dataSource = dataSource;
        this.entityManagerFactory = entityManagerFactory;
        this.transactionManager = new JpaTransactionManager(this.entityManagerFactory);
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        return this.transactionManager;
    }
}
