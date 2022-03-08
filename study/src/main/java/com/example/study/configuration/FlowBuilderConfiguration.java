package com.example.study.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FlowBuilderConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job flowBuilderJob() {
        return jobBuilderFactory.get("flowBuilderJob")
                .incrementer(new RunIdIncrementer())
                .start(flowBuilderFlow())
                .next(flowBuilderRunBatch())
                .end()
                .build();
    }

    @Bean
    public Step flowBuilderRunBatch() {
        return stepBuilderFactory.get("flowBuilderRunBatch")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("Run Batch Finish");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    @Bean
    public Flow flowBuilderFlow() {
        return new FlowBuilder<Flow>("flowBuilderFlow")
                .start(flowBuilderLoadFileStep())
                .next(flowBuilderLoadCustomerStep())
                .build();
    }

    @Bean
    public Step flowBuilderLoadFileStep() {
        return stepBuilderFactory.get("flowBuilderLoadFileStep")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("File Load Finish");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    @Bean
    public Step flowBuilderLoadCustomerStep() {
        return stepBuilderFactory.get("flowBuilderLoadCustomerStep")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("Customer Load Finish");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }
}

