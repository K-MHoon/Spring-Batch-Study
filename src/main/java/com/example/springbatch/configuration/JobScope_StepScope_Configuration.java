package com.example.springbatch.configuration;

import com.example.springbatch.listener.CustomStepListener;
import com.example.springbatch.listener.JobStepScopeListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JobScope_StepScope_Configuration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job jobStepScopeJob() {
        return jobBuilderFactory.get("jobStepScopeJob")
                .start(jobStepScopeStep1(null))
                .next(jobStepScopeStep2())
                .listener(new JobStepScopeListener())
                .build();
    }

    @Bean
    @JobScope
    public Step jobStepScopeStep1(@Value("#{jobParameters['message']}") String message) {
        System.out.println("message = " + message);
        return stepBuilderFactory.get("jobStepScopeStep1")
                .tasklet(tasklet1(null))
                .build();
    }

    @Bean
    public Step jobStepScopeStep2() {
        return stepBuilderFactory.get("jobStepScopeStep2")
                .tasklet(tasklet2(null))
                .listener(new CustomStepListener())
                .build();
    }

    @Bean
    @StepScope
    public Tasklet tasklet1(@Value("#{jobExecutionContext['name']}") String name) {
        System.out.println("name = " + name);
        return (stepContribution, chunkContext) -> {
            System.out.println("tasklet1 has executed");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    @StepScope
    public Tasklet tasklet2(@Value("#{stepExecutionContext['name2']}") String name2) {
        System.out.println("name2 = " + name2);
        return (stepContribution, chunkContext) -> {
            System.out.println("tasklet2 has executed");
            return RepeatStatus.FINISHED;
        };
    }

}
