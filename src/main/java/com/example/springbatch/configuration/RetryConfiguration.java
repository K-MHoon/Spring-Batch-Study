package com.example.springbatch.configuration;

import com.example.springbatch.exception.RetryableException;
import com.example.springbatch.processor.RetryItemProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Retryable;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RetryConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job retryJob() {
        return jobBuilderFactory.get("retryJob")
                .incrementer(new RunIdIncrementer())
                .start(retryStep())
                .build();
    }

    @Bean
    public Step retryStep() {
        return stepBuilderFactory.get("retryStep")
                .<String, String>chunk(5)
                .reader(retryReader())
                .processor(retryProcessor())
                .writer(items -> items.forEach(item -> System.out.println(item)))
                .faultTolerant()
                .retry(RetryableException.class)
                .retryLimit(2)
                .build();
    }

    @Bean
    public ItemProcessor<? super String, String> retryProcessor() {

        return new RetryItemProcessor();
    }

    @Bean
    public ItemReader<String> retryReader() {
        List<String> items = new ArrayList<>();
        for(int i = 0; i < 30; i++) {
            items.add(String.valueOf(i));
        }
        return new ListItemReader<>(items);
    }
}
