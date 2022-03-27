package com.example.study.configuration;

import com.example.study.configurer.HibernateBatchConfigurer;
import com.example.study.dto.WriteCustomer;
import com.example.study.setter.CustomerItemPreparedStatementSetter;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.HibernateItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.HibernateItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class DatabaseWriterConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job databaseWriterJob() {
        return jobBuilderFactory.get("databaseWriterJob")
                .incrementer(new RunIdIncrementer())
                .start(databaseWriterStep())
                .build();
    }

    @Bean
    public Step databaseWriterStep() {
        return stepBuilderFactory.get("databaseWriterStep")
                .<WriteCustomer, WriteCustomer>chunk(5)
                .reader(databaseWriterReaderByJdbc(null))
                .writer(databaseWriterHibernateItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<WriteCustomer> databaseWriterReaderByJdbc(
            @Value("#{jobParameters['customerFile']}") FileSystemResource inputFile) {
        return new FlatFileItemReaderBuilder<WriteCustomer>()
                .name("databaseWriterReaderByJdbc")
                .delimited()
                .names("firstName", "middleInitial", "lastName",
                        "address", "city", "state", "zipCode")
                .targetType(WriteCustomer.class)
                .resource(inputFile)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<WriteCustomer> databaseWriterByJdbc() {
        return new JdbcBatchItemWriterBuilder<WriteCustomer>()
                .dataSource(dataSource)
                .sql("insert into tbl_customer (firstName, " +
                        "middleInitial, " +
                        "lastName, " +
                        "address, " +
                        "city, " +
                        "state, " +
                        "zipCode) VALUES (?,?,?,?,?,?,?)")
                .itemPreparedStatementSetter(new CustomerItemPreparedStatementSetter())
                .build();
    }

    @Bean
    public HibernateItemWriter<WriteCustomer> databaseWriterHibernateItemWriter() {
        return new HibernateItemWriterBuilder<WriteCustomer>()
                .sessionFactory(entityManagerFactory.unwrap(SessionFactory.class))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<WriteCustomer> databaseWriterByNamedParameter() {
        return new JdbcBatchItemWriterBuilder<WriteCustomer>()
                .dataSource(dataSource)
                .sql("insert into tbl_customer (firstName, " +
                        "middleInitial, " +
                        "lastName, " +
                        "address, " +
                        "city, " +
                        "state, " +
                        "zipCode) VALUES (:firstName, " +
                        ":middleInitial, " +
                        ":lastName, " +
                        ":address, " +
                        ":city, " +
                        ":state, " +
                        ":zipCode)")
                .beanMapped()
                .build();
    }
}
