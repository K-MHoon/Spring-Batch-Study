package com.example.study.configuration;

import com.example.study.increment.CustomJobParametersIncrementer;
import com.example.study.validator.CustomJobParametersValidator;
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
public class ValidateConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job validateJob() {
        return this.jobBuilderFactory.get("validateJob")
                .start(validateStep1())
                .next(validateStep2())
                .validator(new CustomJobParametersValidator())
//                .validator(new DefaultJobParametersValidator(new String[]{"name","date"}, new String[]{"count"}))
//                .preventRestart()
                .incrementer(new CustomJobParametersIncrementer())
                .build();
    }

    @Bean
    public Step validateStep1() {
        return this.stepBuilderFactory.get("validateStep1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("validate Step1 was executed");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    @Bean
    public Step validateStep2() {
        return this.stepBuilderFactory.get("validateStep2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("validate Step2 was executed");
//                        throw new RuntimeException("validate Step2 was failed");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }


}
