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
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class JpaPagingConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;
    private int chunkSize = 10;

    @Bean
    public Job jpaPagingJob() {
        return jobBuilderFactory.get("jpaPagingJob")
                .incrementer(new RunIdIncrementer())
                .start(jpaPagingStep1())
                .build();
    }

    @Bean
    public Step jpaPagingStep1() {
        return stepBuilderFactory.get("jpaPagingStep1")
                .<Customer4, Customer4>chunk(chunkSize)
                .reader(jpaPagingItemReader())
                .writer(jpaPagingItemWriter())
                .build();
    }

    @Bean
    public ItemReader<Customer4> jpaPagingItemReader() {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("firstName", "A%");

        return new JpaPagingItemReaderBuilder<Customer4>()
                .name("jpaPagingItemReader")
                .pageSize(chunkSize)
                .entityManagerFactory(entityManagerFactory)
                .queryString("select c from Customer4 c where firstName like :firstName")
                .parameterValues(parameters)
                .build();
    }

    @Bean
    public ItemWriter<? super Customer4> jpaPagingItemWriter() {
        return items -> {
            for (Customer4 item : items) {
                System.out.println("item = " + item.toString());
            }
        };
    }


}
