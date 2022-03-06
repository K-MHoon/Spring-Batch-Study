package com.example.study.configuration;

import com.example.study.decider.CustomDecider;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JobExecutionDeciderConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job JobExecutionDeciderJob() {
        return this.jobBuilderFactory.get("JobExecutionDeciderJob")
                .incrementer(new RunIdIncrementer())
                .start(JobExecutionDeciderStep())
                .next(decider())
                .from(decider()).on("ODD").to(oddStep())
                .from(decider()).on("EVEN").to(evenStep())
                .end()
                .build();
    }

    @Bean
    public JobExecutionDecider decider() {
        return new CustomDecider();
    }

    @Bean
    public Step JobExecutionDeciderStep() {
        return this.stepBuilderFactory.get("JobExecutionDeciderStep")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("[JobExecutionDeciderStep] This is the start Tasklet");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step evenStep() {
        return this.stepBuilderFactory.get("evenStep")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("evenStep has executed");
                    return RepeatStatus.FINISHED;
                }).build();
    }

    @Bean
    public Step oddStep() {
        return this.stepBuilderFactory.get("oddStep")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("oddStep has executed");
                    return RepeatStatus.FINISHED;
                }).build();
    }
}
