package com.example.study.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class CompositeCompletionPolicyConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job compositeCompletionPolicyJob() {
        return jobBuilderFactory.get("compositeCompletionPolicyJob")
                .incrementer(new RunIdIncrementer())
                .start(compositeCompletionPolicyStep())
                .build();
    }

    @Bean
    @JobScope
    public Step compositeCompletionPolicyStep() {
        return stepBuilderFactory.get("compositeCompletionPolicyStep")
                .<String, String>chunk(compositeCompletionPolicyChunk())
                .reader(compositeCompletionPolicyItemReader())
                .writer(compositeCompletionPolicyItemWriter())
                .build();
    }

    @Bean
    public CompletionPolicy compositeCompletionPolicyChunk() {
        CompositeCompletionPolicy compositeCompletionPolicy = new CompositeCompletionPolicy();
        compositeCompletionPolicy.setPolicies(
                new CompletionPolicy[] {
                        new TimeoutTerminationPolicy(3),
                        new SimpleCompletionPolicy(1000)
                }
        );
        return compositeCompletionPolicy;
    }

    @Bean
    public ItemWriter<? super String> compositeCompletionPolicyItemWriter() {
        return items -> {
            for (String item : items) {
                System.out.println("current Item = " + item);
            }
        };
    }

    @Bean
    public ListItemReader<String> compositeCompletionPolicyItemReader() {
        List<String> items = new ArrayList<>(100000);

        for (int i = 0; i < 100000; i++) {
            items.add(UUID.randomUUID().toString());
        }

        return new ListItemReader<>(items);
    }


}
