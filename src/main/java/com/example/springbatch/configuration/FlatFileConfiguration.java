package com.example.springbatch.configuration;

import com.example.springbatch.dto.Customer2;
import com.example.springbatch.linemapper.DefaultLineMapper;
import com.example.springbatch.mapper.CustomerFieldSetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class FlatFileConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job flatFileJob() {
        return jobBuilderFactory.get("flatFileJob")
                .start(flatFileStep1())
                .next(flatFileStep2())
                .build();
    }

    @Bean
    public Step flatFileStep1() {
        return stepBuilderFactory.get("flatFileStep1")
                .chunk(5)
                .reader(flatFileItemReader())
                .writer(new ItemWriter() {
                    @Override
                    public void write(List list) throws Exception {
                        System.out.println("items = " + list);
                    }
                })
                .build();
    }

    @Bean
    public ItemReader flatFileItemReader() {

        FlatFileItemReader<Customer2> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new ClassPathResource("/customer.csv"));

        DefaultLineMapper<Customer2> lineMapper = new DefaultLineMapper<>();
        lineMapper.setTokenizer(new DelimitedLineTokenizer());
        lineMapper.setFieldSetMapper(new CustomerFieldSetMapper());

        itemReader.setLineMapper(lineMapper);
        // 첫 라인은 읽지 않기
        itemReader.setLinesToSkip(1);

        return itemReader;
    }

    @Bean
    public Step flatFileStep2() {
        return stepBuilderFactory.get("flatFileStep2")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("flatFileStep2 has executed");
                        return RepeatStatus.FINISHED;
                    }
                })
                .build();
    }


}
