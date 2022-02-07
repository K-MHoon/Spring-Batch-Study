package com.example.springbatch.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
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
public class TransitionConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job transitionJob() {
        return this.jobBuilderFactory.get("transitionJob")
                .start(transitionStep1())
                    .on("FAILED")
                    .to(transitionStep2())
                    .on("FAILED")
                    .stop()
                .from(transitionStep1()).on("*")
                    .to(transitionStep3())
                    .next(transitionStep4())
                .from(transitionStep2()).on("*")
                    .to(transitionStep5())
                    .end()
                .build();
    }

    @Bean
    public Step transitionStep1() {
        return this.stepBuilderFactory.get("transitionStep1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("transitionStep1 was executed");
                        stepContribution.setExitStatus(ExitStatus.FAILED);
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    @Bean
    public Step transitionStep2() {
        return this.stepBuilderFactory.get("transitionStep2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("transitionStep2 was executed");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    @Bean
    public Step transitionStep3() {
        return this.stepBuilderFactory.get("transitionStep3")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("transitionStep3 was executed");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    @Bean
    public Step transitionStep4() {
        return this.stepBuilderFactory.get("transitionStep4")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("transitionStep4 was executed");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    @Bean
    public Step transitionStep5() {
        return this.stepBuilderFactory.get("transitionStep5")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("transitionStep5 was executed");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }
}
