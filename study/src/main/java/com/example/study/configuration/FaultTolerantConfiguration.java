package com.example.study.configuration;

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
public class FaultTolerantConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job faultTolerantJob() {
        return jobBuilderFactory.get("faultTolerantJob")
                .incrementer(new RunIdIncrementer())
                .start(faultTolerantStep())
                .build();
    }

    @Bean
    public Step faultTolerantStep() {
        return stepBuilderFactory.get("faultTolerantStep")
                .<String, String>chunk(5)
                .reader(new ItemReader<String>() {

                    int i = 0;

                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        i++;

                        if(i == 1) {
                            throw new IllegalArgumentException("this exception is skipped");
                        }

                        return i > 3 ? null : "item" + i;
                    }
                })
                .processor(new ItemProcessor<String, String>() {
                    @Override
                    public String process(String item) throws Exception {
                        throw new IllegalStateException("this exception is retried");
                    }
                })
                .writer(items -> System.out.println(items))
                .faultTolerant()
                .skip(IllegalArgumentException.class)
                .skipLimit(2)
                .retry(IllegalStateException.class)
                .retryLimit(2)
                .build();
    }


}
