package com.example.study.configuration;

import com.example.study.listener.CustomAnnotationJobExecutionListener;
import com.example.study.listener.CustomStepExecutionListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JobAndStepListenerConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final CustomStepExecutionListener stepExecutionListener;

    @Bean
    public Job jobAndStepListenerJob() {
        return jobBuilderFactory.get("jobAndStepListenerJob")
                .incrementer(new RunIdIncrementer())
                .start(jobAndStepListenerStep1())
                .next(jobAndStepListenerStep2())
//                .listener(new CustomJobExecutionListener())
                .listener(new CustomAnnotationJobExecutionListener())
                .build();
    }

    @Bean
    public Step jobAndStepListenerStep1() {
        return stepBuilderFactory.get("jobAndStepListenerStep1")
                .tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED)
                .listener(stepExecutionListener)
                .build();
    }

    @Bean
    public Step jobAndStepListenerStep2() {
        return stepBuilderFactory.get("jobAndStepListenerStep2")
                .tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED)
                .listener(stepExecutionListener)
                .build();
    }
}
