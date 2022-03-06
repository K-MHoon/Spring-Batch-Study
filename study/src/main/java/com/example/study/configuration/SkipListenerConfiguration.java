package com.example.study.configuration;

import com.example.study.exception.SkippableException;
import com.example.study.listener.CustomSkipListener;
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
public class SkipListenerConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job skipListenerJob() {
        return jobBuilderFactory.get("skipListenerJob")
                .incrementer(new RunIdIncrementer())
                .start(skipListenerStep())
                .build();
    }

    @Bean
    public Step skipListenerStep() {
        return stepBuilderFactory.get("skipListenerStep")
                .<Integer, String>chunk(10)
                .reader(new ItemReader<Integer>() {
                    private int cnt;
                    @Override
                    public Integer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        cnt++;
                        if(cnt == 3) {
                            throw new SkippableException("read skipped : " + cnt);
                        }

                        return cnt <= 20 ? cnt : null;
                    }
                })
                .processor(new ItemProcessor<Integer, String>() {
                    @Override
                    public String process(Integer item) throws Exception {

                        if(item == 4) {
                            throw new SkippableException("process skipped");
                        }
                        return "item" + item;
                    }
                })
                .writer(new ItemWriter<String>() {
                    @Override
                    public void write(List<? extends String> items) throws Exception {
                        for (String item : items) {
                            if(item.equals("item5")) {
                                throw new SkippableException("write skipped");
                            }
                            System.out.println("item = " + item);
                        }
                    }
                })
                .faultTolerant()
                .skip(SkippableException.class)
                .skipLimit(3)
                .listener(new CustomSkipListener())
                .build();
    }
}
