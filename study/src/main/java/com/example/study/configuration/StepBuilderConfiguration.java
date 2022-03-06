package com.example.study.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.*;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class StepBuilderConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job stepBuilderJob() {
        return this.jobBuilderFactory.get("stepBuilderJob")
                .incrementer(new RunIdIncrementer())
                .start(stepBuilderStep1())
                .next(stepBuilderStep2())
                .next(stepBuilderStep3())
                .build();
    }

    @Bean
    public Step stepBuilderStep1() {
        return this.stepBuilderFactory.get("stepBuilderStep1")
                .tasklet((stepContribution, chunkContext) ->  {
                    System.out.println("step1 has executed");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }


    @Bean
    public Step stepBuilderStep2() {
        return this.stepBuilderFactory.get("stepBuilderStep2")
                .<String, String>chunk(3)
                .reader(new ItemReader<String>() {
                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        return null;
                    }
                })
                .processor(new ItemProcessor<String, String>() {
                    @Override
                    public String process(String s) throws Exception {
                        return null;
                    }
                })
                .writer(new ItemWriter<String>() {
                    @Override
                    public void write(List<? extends String> list) throws Exception {

                    }
                })
                .build();
    }


    @Bean
    public Step stepBuilderStep3() {
        return this.stepBuilderFactory.get("stepBuilderStep3")
                .partitioner(stepBuilderStep1())
                .gridSize(2)
                .build();
    }


    @Bean
    public Step stepBuilderStep4() {
        return this.stepBuilderFactory.get("stepBuilderStep4")
                .job(job())
                .build();
    }


    @Bean
    public Step stepBuilderStep5() {
        return this.stepBuilderFactory.get("stepBuilderStep5")
                .flow(stepBuilderFlow())
                .build();
    }

    @Bean
    public Job job() {
        return this.jobBuilderFactory.get("job")
                .start(stepBuilderStep1())
                .next(stepBuilderStep2())
                .next(stepBuilderStep3())
                .build();
    }

    @Bean
    public Flow stepBuilderFlow() {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("flow");
        flowBuilder.start(stepBuilderStep2()).end();
        return flowBuilder.build();
    }
}
