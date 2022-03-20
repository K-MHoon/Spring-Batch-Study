package com.example.study.configuration;

import com.example.study.dto.MyCustomer5;
import com.example.study.repository.MyCustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class RepositoryConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final MyCustomerRepository customerRepository;

    @Bean
    public Job repositoryJob() {
        return jobBuilderFactory.get("repositoryJob")
                .incrementer(new RunIdIncrementer())
                .start(repositoryStep())
                .build();
    }

    @Bean
    public Step repositoryStep() {
        return stepBuilderFactory.get("repositoryStep")
                .<MyCustomer5, MyCustomer5>chunk(10)
                .reader(repositoryItemReader(null))
                .writer((items) -> items.forEach(System.out::println))
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<MyCustomer5> repositoryItemReader(
            @Value("#{jobParameters['city']}") String city
    ) {
        return new RepositoryItemReaderBuilder<MyCustomer5>()
                .name("repositoryItemReader")
                .arguments(Collections.singletonList(city))
                .methodName("findByCity")
                .repository(customerRepository)
                .sorts(Collections.singletonMap("lastName", Sort.Direction.ASC))
                .build();
    }
}
