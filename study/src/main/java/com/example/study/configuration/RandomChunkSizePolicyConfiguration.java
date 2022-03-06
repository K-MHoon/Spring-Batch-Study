package com.example.study.configuration;

import com.example.study.policy.RandomChunkSizePolicy;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class RandomChunkSizePolicyConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job randomChunkSizePolicyJob() {
        return jobBuilderFactory.get("randomChunkSizePolicyJob")
                .incrementer(new RunIdIncrementer())
                .start(randomChunkSizePolicyStep())
                .build();
    }

    @Bean
    @JobScope
    public Step randomChunkSizePolicyStep() {
        return stepBuilderFactory.get("randomChunkSizePolicyStep")
                .<String, String>chunk(randomChunkSizePolicyChunk())
                .reader(randomChunkSizePolicyItemReader())
                .writer(randomChunkSizePolicyItemWriter())
                .build();
    }

    @Bean
    public CompletionPolicy randomChunkSizePolicyChunk() {
        return new RandomChunkSizePolicy();
    }

    @Bean
    public ItemWriter<? super String> randomChunkSizePolicyItemWriter() {
        return items -> {
            for (String item : items) {
                System.out.println("current Item = " + item);
            }
        };
    }

    @Bean
    public ListItemReader<String> randomChunkSizePolicyItemReader() {
        List<String> items = new ArrayList<>(100);

        for (int i = 0; i < 100; i++) {
            items.add(UUID.randomUUID().toString());
        }

        return new ListItemReader<>(items);
    }


}
