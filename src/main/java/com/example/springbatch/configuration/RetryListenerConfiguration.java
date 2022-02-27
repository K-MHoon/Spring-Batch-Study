package com.example.springbatch.configuration;

import com.example.springbatch.exception.RetryableException;
import com.example.springbatch.listener.CustomRetryListener;
import com.example.springbatch.processor.RetryCustomItemProcessor;
import com.example.springbatch.writer.RetryCustomItemWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RetryListenerConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job retryListenerJob() {
        return jobBuilderFactory.get("retryListenerJob")
                .incrementer(new RunIdIncrementer())
                .start(retryListenerStep())
                .build();
    }

    @Bean
    public Step retryListenerStep() {
        return stepBuilderFactory.get("retryListenerStep")
                .<Integer, String>chunk(10)
                .reader(new ItemReader<Integer>() {
                    private int cnt;
                    @Override
                    public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        cnt++;
                        return cnt <= 20 ? cnt : null;
                    }
                })
                .processor(new RetryCustomItemProcessor())
                .writer(new RetryCustomItemWriter())
                .faultTolerant()
                .retry(RetryableException.class)
                .retryLimit(2)
                .listener(new CustomRetryListener())
                .build();
    }
}
