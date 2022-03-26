package com.example.study.configuration;

import com.example.study.dto.MyCustomer;
import com.example.study.dto.WriteCustomer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@RequiredArgsConstructor
@Configuration
public class FlatFileWriterConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job flatFileWriterJob() {
        return jobBuilderFactory.get("flatFileWriterJob")
                .incrementer(new RunIdIncrementer())
                .start(flatFileWriterStep())
                .build();
    }

    @Bean
    public Step flatFileWriterStep() {
        return stepBuilderFactory.get("flatFileWriterStep")
                .<WriteCustomer, WriteCustomer>chunk(5)
                .reader(flatFileItemReaderWriteCustomer(null))
                .writer(flatFileItemWriterByFormat(null))
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<WriteCustomer> flatFileItemReaderWriteCustomer(
            @Value("#{jobParameters['customerFile']}") FileSystemResource inputFile) {
        return new FlatFileItemReaderBuilder<WriteCustomer>()
                .name("flatFileItemReaderWriteCustomer")
                .delimited()
                .names("firstName", "middleInitial", "lastName",
                        "address", "city", "state", "zip")
                .targetType(WriteCustomer.class)
                .resource(inputFile)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<WriteCustomer> flatFileItemWriterByFormat(
            @Value("#{jobParameters['outputFile']}") FileSystemResource outputFile) {
        return new FlatFileItemWriterBuilder<WriteCustomer>()
                .name("flatFileItemWriterByFormat")
                .resource(outputFile)
                .formatted()
                .format("%s %s lives at %s %s in %s, %s.")
                .names("firstName", "middleInitial", "lastName",
                        "address", "city", "state", "zip")
                .build();
    }

}
