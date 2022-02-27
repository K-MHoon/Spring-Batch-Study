package com.example.springbatch.configuration;

import com.example.springbatch.listener.CustomChunkListener;
import com.example.springbatch.listener.CustomItemProcessListener;
import com.example.springbatch.listener.CustomItemReadListener;
import com.example.springbatch.listener.CustomItemWriteListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ChunkListenerConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job chunkListenerJob() {
        return jobBuilderFactory.get("chunkListenerJob")
                .incrementer(new RunIdIncrementer())
                .start(chunkListenerStep())
                .build();
    }

    @Bean
    public Step chunkListenerStep() {
        return stepBuilderFactory.get("chunkListenerStep")
                .<Integer, String>chunk(10)
                .listener(new CustomChunkListener())
                .reader(new ItemReader<Integer>() {
                    private int cnt;
                    @Override
                    public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        cnt++;
                        return cnt <= 20 ? cnt : null;
                    }
                })
                .listener(new CustomItemReadListener())
                .processor(new ItemProcessor() {
                    @Override
                    public Object process(Object item) throws Exception {
//                        return new RuntimeException("error!");
                        return "item" + item;
                    }
                })
                .listener(new CustomItemProcessListener())
                .writer(new ItemWriter() {
                    @Override
                    public void write(List items) throws Exception {
                        System.out.println("items = " + items);
                    }
                })
                .listener(new CustomItemWriteListener())
                .build();
    }
}
