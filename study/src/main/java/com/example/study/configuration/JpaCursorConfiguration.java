package com.example.study.configuration;

import com.example.study.dto.Customer4;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class JpaCursorConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private int chunkSize = 10;

    @Bean
    public Job jpaCursorJob() {
        return jobBuilderFactory.get("jpaCursorJob")
                .incrementer(new RunIdIncrementer())
                .start(jpaCursorStep1())
                .build();
    }

    @Bean
    public Step jpaCursorStep1() {
        return stepBuilderFactory.get("jpaCursorStep1")
                .<Customer4, Customer4>chunk(chunkSize)
                .reader(jpaCursorItemReader())
                .writer(jpaCursorItemWriter())
                .build();
    }

    @Bean
    public ItemReader<Customer4> jpaCursorItemReader() {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("firstName", "A%");

        return new JpaCursorItemReaderBuilder<Customer4>()
                .name("jpaCursorItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select c from Customer4 c where firstName like :firstName")
                .parameterValues(parameters)
                .build();
    }

    @Bean
    public ItemWriter<? super Customer4> jpaCursorItemWriter() {
        return items -> {
            for (Customer4 item : items) {
                System.out.println("item = " + item.toString());
            }
        };
    }

}
