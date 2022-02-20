package com.example.springbatch.configuration;

import com.example.springbatch.processor.ComPositionItemProcessor;
import com.example.springbatch.processor.ComPositionItemProcessor2;
import com.example.springbatch.processor.CustomItemProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class CompositionItemConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job comPositionItemJob() {
        return jobBuilderFactory.get("comPositionItemJob")
                .incrementer(new RunIdIncrementer())
                .start(comPositionItemStep())
                .build();
    }

    @Bean
    public Step comPositionItemStep() {
        return stepBuilderFactory.get("comPositionItemStep")
                .<String, String>chunk(10)
                .reader(new ItemReader<String>() {

                    int i = 0;

                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        i++;
                        return i > 10 ? null : "item" + i;
                    }
                })
                .processor(comPositionItemProcessor())
                .writer(items -> System.out.println(items))
                .build();
    }

    @Bean
    public ItemProcessor<? super String, String> comPositionItemProcessor() {
        List itemProcessor = new ArrayList();
        itemProcessor.add(new ComPositionItemProcessor());
        itemProcessor.add(new ComPositionItemProcessor2());

        return new CompositeItemProcessorBuilder<>()
                .delegates(itemProcessor)
                .build();
    }

}
