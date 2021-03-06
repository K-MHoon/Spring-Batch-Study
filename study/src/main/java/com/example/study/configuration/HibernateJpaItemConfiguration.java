package com.example.study.configuration;

import com.example.study.dto.MyCustomer5;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.HibernateCursorItemReader;
import org.springframework.batch.item.database.HibernatePagingItemReader;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.HibernateCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.HibernatePagingItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class HibernateJpaItemConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job hibernateJpaItemJob() throws Exception {
        return jobBuilderFactory.get("hibernateJpaItemJob")
                .incrementer(new RunIdIncrementer())
                .start(hibernateJpaItemStep())
                .build();
    }

    @Bean
    public Step hibernateJpaItemStep() throws Exception {
        return stepBuilderFactory.get("hibernateJpaItemStep")
                .<MyCustomer5, MyCustomer5>chunk(10)
                .reader(hibernateJpaPagingItemReader(null))
                .writer(hibernateJpaItemWriter())
                .build();
    }

    @Bean
    public ItemWriter hibernateJpaItemWriter() {
        return (items) -> items.forEach(System.out::println);
    }
    @Bean
    @StepScope
    public HibernateCursorItemReader<MyCustomer5> hibernateCursorItemReader(
            @Value("#{jobParameters['city']}") String city
    ) {
        return new HibernateCursorItemReaderBuilder<MyCustomer5>()
                .name("hibernateCursorItemReader")
                .sessionFactory(entityManagerFactory.unwrap(SessionFactory.class))
                .queryString("from Customer where city = :city")
                .parameterValues(Collections.singletonMap("city", city))
                .build();
    }

    @Bean
    @StepScope
    public HibernatePagingItemReader<MyCustomer5> hibernatePagingItemReader(
            @Value("#{jobParameters['city']}") String city
    ) {
        return new HibernatePagingItemReaderBuilder<MyCustomer5>()
                .name("hibernatePagingItemReader")
                .sessionFactory(entityManagerFactory.unwrap(SessionFactory.class))
                .queryString("from Customer where city = :city")
                .parameterValues(Collections.singletonMap("city", city))
                .pageSize(10)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<MyCustomer5> hibernateJpaPagingItemReader(@Value("#{jobParameters['city']}") String city) {
        return new JpaPagingItemReaderBuilder<MyCustomer5>()
                .name("hibernateJpaPagingItemReader")
                .parameterValues(Collections.singletonMap("city", city))
                .entityManagerFactory(entityManagerFactory)
                .queryString("select c from Customer c where c.city = :city")
                .build();
    }
}
