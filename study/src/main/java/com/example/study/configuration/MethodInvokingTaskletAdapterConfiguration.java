package com.example.study.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.MethodInvokingTaskletAdapter;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 기존의 서비스를 Tasklet으로 활용하기
 */
@Configuration
@RequiredArgsConstructor
public class MethodInvokingTaskletAdapterConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job methodInvokingTaskletAdapterJob() {
        return jobBuilderFactory.get("methodInvokingTaskletAdapterJob")
                .incrementer(new RunIdIncrementer())
                .start(methodInvokingTaskletAdapterStep())
                .build();
    }

    @Bean
    @JobScope
    public Step methodInvokingTaskletAdapterStep() {
        return stepBuilderFactory.get("methodInvokingTaskletAdapterStep")
                .tasklet(methodInvokingTaskletAdapterTasklet(null))
                .build();
    }

    @Bean
    @StepScope
    public Tasklet methodInvokingTaskletAdapterTasklet(@Value("#{jobParameters['message']}") String message) {
        MethodInvokingTaskletAdapter methodInvokingTaskletAdapter = new MethodInvokingTaskletAdapter();
        methodInvokingTaskletAdapter.setTargetObject(methodInvokingTaskletService());
        methodInvokingTaskletAdapter.setTargetMethod("serviceMethod");
        methodInvokingTaskletAdapter.setArguments(new String[]{message});
        return methodInvokingTaskletAdapter;
    }

    @Bean
    public MethodInvokingTaskletService methodInvokingTaskletService() {
        return new MethodInvokingTaskletService();
    }


    private class MethodInvokingTaskletService {

        public void serviceMethod(String message) {
            System.out.println(message);
        }
    }
}
