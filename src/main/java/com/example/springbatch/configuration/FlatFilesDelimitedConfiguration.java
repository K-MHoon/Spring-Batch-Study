package com.example.springbatch.configuration;

import com.example.springbatch.dto.Customer3;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class FlatFilesDelimitedConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job flatFilesDelimitedJob() {
        return jobBuilderFactory.get("flatFilesDelimitedJob")
                .incrementer(new RunIdIncrementer())
                .start(flatFilesDelimitedStep1())
                .build();
    }

    @Bean
    public Step flatFilesDelimitedStep1() {
        return stepBuilderFactory.get("flatFilesDelimitedStep1")
                .<Customer3, Customer3>chunk(10)
                .reader(flatFilesDelimitedReader())
                .writer(flatFilesDelimitedWriter())
                .build();
    }

    @Bean
    public ItemWriter<? super Customer3> flatFilesDelimitedWriter() {
        return new FlatFileItemWriterBuilder<>()
                .name("flatFilesDelimitedWriter")
                .resource(new FileSystemResource("C:\\Users\\MHK\\Desktop\\SpringBatch\\src\\main\\resources\\customer3.txt"))
                .append(true)
                .shouldDeleteIfEmpty(true)
                .delimited()
                .delimiter("|")
                .names(new String[]{"id", "name", "age"})
                .build();
    }

    @Bean
    public ItemReader<? extends Customer3> flatFilesDelimitedReader() {

        List<Customer3> customers = Arrays.asList(new Customer3(1L, "hong gil dong1", 41),
                new Customer3(2L, "hong gil dong2", 42),
                new Customer3(3L, "hong gil dong3", 45));

        ListItemReader<Customer3> reader = new ListItemReader<>(customers);

        return reader;
    }


}
