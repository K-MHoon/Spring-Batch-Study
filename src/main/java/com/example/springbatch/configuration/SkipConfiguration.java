package com.example.springbatch.configuration;

import com.example.springbatch.exception.NoSkippableException;
import com.example.springbatch.exception.SkippableException;
import com.example.springbatch.processor.SkipItemProcessor;
import com.example.springbatch.writer.SkipItemWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.core.step.skip.LimitCheckingItemSkipPolicy;
import org.springframework.batch.core.step.skip.NeverSkipItemSkipPolicy;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SkipConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job skipJob() {
        return jobBuilderFactory.get("skipJob")
                .incrementer(new RunIdIncrementer())
                .start(skipStep())
                .build();
    }

    @Bean
    public Step skipStep() {
        return stepBuilderFactory.get("skipStep")
                .<String, String>chunk(5)
                .reader(new ItemReader<String>() {
                    int i = 0;
                    @Override
                    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
                        i++;
                        if(i == 3) {
                            throw new SkippableException("this exception is skipped");
                        }
                        System.out.println("ItemReader : " + i);
                        return i > 20 ? null : String.valueOf(i);
                    }
                })
                .processor(skipProcessor())
                .writer(skipWriter())
                .faultTolerant()
//                .noSkip(NoSkippableException.class)
//                .skipLimit(4)
                .skipPolicy(limitCheckingItemSkipPolicy())
                .build();
    }

    /**
     * SkipPolicy를 직접 설정
     * @return
     */
    @Bean
    public SkipPolicy limitCheckingItemSkipPolicy() {

        Map<Class<? extends  Throwable>, Boolean> exceptionClass =
                Map.ofEntries(
                        Map.entry(SkippableException.class, true)
                );
        LimitCheckingItemSkipPolicy limitCheckingItemSkipPolicy = new LimitCheckingItemSkipPolicy(4, exceptionClass);
        return limitCheckingItemSkipPolicy;
    }

    @Bean
    public ItemWriter<? super String> skipWriter() {
        return new SkipItemWriter();
    }

    @Bean
    public ItemProcessor<? super String, String> skipProcessor() {
        return new SkipItemProcessor();
    }



}
