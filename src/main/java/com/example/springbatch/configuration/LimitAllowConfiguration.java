package com.example.springbatch.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LimitAllowConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job LimitAllowJob() {
        return this.jobBuilderFactory.get("limitAllowJob")
                .start(limitAllowStep1())
                .next(limitAllowStep2())
                .build();
    }

    @Bean
    public Step limitAllowStep1() {
        return this.stepBuilderFactory.get("limitAllowStep1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("limit Allow Step1 was executed");
                        return RepeatStatus.FINISHED;
                    }
                })
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step limitAllowStep2() {
        return this.stepBuilderFactory.get("limitAllowStep2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        throw new RuntimeException("limit Allow Step was failed");
                    }
                })
                .startLimit(3)
                .build();
    }
}
