package com.example.study.configuration;

import com.example.study.dto.Customer4;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class JdbcCursorConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private int chunkSize = 10;
    private final DataSource dataSource;

    @Bean
    public Job jdbcCursorJob() {
        return jobBuilderFactory.get("jdbcCursorJob")
                .start(jdbcCursorStep1())
                .build();
    }

    @Bean
    public Step jdbcCursorStep1() {
        return stepBuilderFactory.get("jdbcCursorStep1")
                .<Customer4, Customer4>chunk(chunkSize)
                .reader(jdbcCursorItemReader())
                .writer(jdbcCursorItemWriter())
                .build();
    }

    @Bean
    public ItemReader<Customer4> jdbcCursorItemReader() {

        return new JdbcCursorItemReaderBuilder<Customer4>()
                .name("jdbcCursorItemReader")
                .fetchSize(chunkSize)
                .sql("select id, firstName, lastName, birthdate from customer where firstName like ? order by lastName, firstName")
                .beanRowMapper(Customer4.class)
                .queryArguments("A%")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public ItemWriter<? super Customer4> jdbcCursorItemWriter() {
        return items -> {
            for (Customer4 item : items) {
                System.out.println("item = " + item.toString());
            }
        };
    }


}
