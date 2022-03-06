package com.example.study.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Callable;

/**+
 * 별도의 스레드에서 실행되는 CallableTasklet. (병렬 X)
 */
@Configuration
@RequiredArgsConstructor
public class CallableTaskletAdapterConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job callableTaskletAdapterJob() {
        return jobBuilderFactory.get("callableTaskletAdapterJob")
                .incrementer(new RunIdIncrementer())
                .start(callableTaskletAdapterStep())
                .build();
    }

    @Bean
    public Step callableTaskletAdapterStep() {
        return stepBuilderFactory.get("callableTaskletAdapterStep")
                .tasklet(callableTaskletAdapterTasklet())
                .build();
    }

    @Bean
    public Tasklet callableTaskletAdapterTasklet() {
        CallableTaskletAdapter callableTaskletAdapter = new CallableTaskletAdapter();
        callableTaskletAdapter.setCallable(callableObject());
        return callableTaskletAdapter;
    }

    @Bean
    public Callable<RepeatStatus> callableObject() {
        return () -> {
            System.out.println("callableObject Tasklet Test");
            return RepeatStatus.FINISHED;
        };
    }

}
