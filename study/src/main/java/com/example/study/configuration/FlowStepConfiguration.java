package com.example.study.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FlowStepConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job flowStepJob() {
        return jobBuilderFactory.get("flowStepJob")
                .start(flowStep())
                .next(flowStep_2())
                .build();
    }

    @Bean
    public Step flowStep() {
        return stepBuilderFactory.get("flowStep")
                .flow(flowStepFlow())
                .build();
    }

    @Bean
    public Step flowStep_1() {
        return stepBuilderFactory.get("flowStep-1")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("flowStep1 was executed");
                        throw new RuntimeException("flowStep1 was failed");
//                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    @Bean
    public Step flowStep_2() {
        return stepBuilderFactory.get("flowStep-2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("flowStep2 was executed");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    @Bean
    public Flow flowStepFlow() {
        FlowBuilder<Flow> flowBuilder  = new FlowBuilder<>("flowStepFlow");
        flowBuilder.start(flowStep_1()).end();

        return flowBuilder.build();
    }
}
