package com.example.study.configuration;

import com.example.study.decider.RandomDecider;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RandomDeciderConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job randomDeciderJob() {
        return jobBuilderFactory.get("randomDecider")
                .incrementer(new RunIdIncrementer())
                .start(randomDeciderStep())
                .next(randomDecider())
                .from(randomDecider()).on("FAILED").to(randomDeciderFailureStep())
                .from(randomDecider()).on("*").to(randomDeciderSuccessStep())
                .end().build();
    }

    @Bean
    public Step randomDeciderFailureStep() {
        return stepBuilderFactory.get("randomDeciderFailureStep")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("Failed!");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    @Bean
    public Step randomDeciderSuccessStep() {
        return stepBuilderFactory.get("randomDeciderSuccessStep")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("success!");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    @Bean
    public JobExecutionDecider randomDecider() {
        return new RandomDecider();
    }

    @Bean
    public Step randomDeciderStep() {
        return stepBuilderFactory.get("randomDeciderStep")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }


}
