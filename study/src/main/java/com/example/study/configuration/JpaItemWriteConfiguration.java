package com.example.study.configuration;

import com.example.study.dto.Customer4;
import com.example.study.dto.Customer5;
import com.example.study.processor.JpaItemWriteProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class JpaItemWriteConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final EntityManagerFactory entityManagerFactory;
    private int chunkSize = 10;

//    @Bean
//    @Qualifier("jpaTrx")
//    public PlatformTransactionManager jpaTransactionManager() {
//        return new JpaTransactionManager(entityManagerFactory);
//    }

    @Bean
    public Job jpaItemWriteJob() {
        return jobBuilderFactory.get("jpaItemWriteJob")
                .incrementer(new RunIdIncrementer())
                .start(jpaItemWriteStep1())
                .build();
    }

    @Bean
    public Step jpaItemWriteStep1() {
        return stepBuilderFactory.get("jpaItemWriteStep1")
//                .transactionManager(jpaTransactionManager())
                .<Customer4, Customer5> chunk(chunkSize)
                .reader(jpaItemWriteReader())
                .processor(jpaItemWriteProcessor())
                .writer(jpaItemWriteWriter())
                .build();
    }


    @Bean
    public ItemProcessor jpaItemWriteProcessor() {
        return new JpaItemWriteProcessor();
    }

    @Bean
    public ItemWriter jpaItemWriteWriter() {
        return new JpaItemWriterBuilder<Customer5>()
                .entityManagerFactory(entityManagerFactory)
                .usePersist(true)
                .build();
    }

    @Bean
    public ItemWriter<Customer5> simpleWriter() {
        return items -> {
            for (Customer5 item : items) {
                System.out.println("item.toString() = " + item.toString());
            }
        };
    }

    @Bean
    public ItemReader jpaItemWriteReader() {

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
        queryProvider.setSelectClause("id, first_name, last_name, birthdate");
        queryProvider.setFromClause("from customer");
        queryProvider.setWhereClause("where first_name like :first_name");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.ASCENDING);
        queryProvider.setSortKeys(sortKeys);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("first_name", "C%");

        return  new JdbcPagingItemReaderBuilder<Customer4>()
                .name("jpaItemWriteReader")
                .dataSource(dataSource)
                .fetchSize(chunkSize)
                .beanRowMapper(Customer4.class)
                .parameterValues(parameters)
                .queryProvider(queryProvider)
                .build();
    }
}
