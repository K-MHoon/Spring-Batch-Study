package com.example.study.configuration;

import com.example.study.dto.Customer3;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@RequiredArgsConstructor
public class JsonConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job jsonJob() {
        return jobBuilderFactory.get("jsonJob")
                .start(jsonStep1())
                .build();
    }

    @Bean
    public Step jsonStep1() {
        return stepBuilderFactory.get("jsonStep1")
                .<Customer3, Customer3>chunk(3)
                .reader(jsonItemReader())
                .writer(jsonItemWriter())
                .build();
    }

    @Bean
    public ItemWriter<? super Customer3> jsonItemWriter() {
        return items -> {
            for (Customer3 item : items) {
                System.out.println(item.toString());
            }
        };
    }

    @Bean
    public ItemReader<? extends Customer3> jsonItemReader() {
        return new JsonItemReaderBuilder<Customer3>()
                .name("jsonReader")
                .resource(new ClassPathResource("/customer.json"))
                .jsonObjectReader(new JacksonJsonObjectReader<>(Customer3.class))
                .build();
    }
}
