package com.example.springbatch.configuration;

import com.example.springbatch.dto.Customer;
import com.example.springbatch.processor.CustomItemProcessor;
import com.example.springbatch.reader.CustomItemReader;
import com.example.springbatch.reader.CustomItemStreamReader;
import com.example.springbatch.reader.CustomItemStreamWriter;
import com.example.springbatch.writer.CustomItemWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class ItemRPWConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job itemRPWJob() {
        return jobBuilderFactory.get("itemRPWJob")
                .start(itemRPWStep3())
                .next(itemRPWStep2())
                .build();
    }

    @Bean
    public Step itemRPWStep2() {
        return stepBuilderFactory.get("itemRPWStep2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("itemRPWStep2 has executed");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }

    @Bean
    public Step itemRPWStep1() {
        return stepBuilderFactory.get("itemRPWStep1")
                .<Customer, Customer>chunk(3)
                .reader(customItemReader())
                .processor(customItemProcessor())
                .writer(customItemWriter())
                .build();
    }

    @Bean
    public Step itemRPWStep3() {
        return stepBuilderFactory.get("itemRPWStep3")
                .<Customer, Customer>chunk(3)
                .reader(customItemStreamReader())
                .writer(customItemStreamWriter())
                .build();
    }

    @Bean
    public ItemWriter<? super Customer> customItemStreamWriter() {
        return new CustomItemStreamWriter();
    }


    @Bean
    public ItemWriter<? super Customer> customItemWriter() {
        return new CustomItemWriter();
    }

    @Bean
    public ItemProcessor<? super Customer, ? extends Customer> customItemProcessor() {
        return new CustomItemProcessor();
    }

    @Bean
    public ItemReader<? extends Customer> customItemReader() {
        return new CustomItemReader(Arrays.asList(new Customer("user1"),
                new Customer("user2"),
                new Customer("user3")));
    }

    @Bean
    public CustomItemStreamReader customItemStreamReader() {
        List<Customer> items = new ArrayList<>(10);

        for(int i = 0; i <= 10; i++) {
            items.add(new Customer("user" + (i+1)));
        }

        return new CustomItemStreamReader(items);
    }




}
