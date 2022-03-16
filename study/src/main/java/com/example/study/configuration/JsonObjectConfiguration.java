package com.example.study.configuration;

import com.example.study.dto.MyCustomer3;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.text.SimpleDateFormat;

@RequiredArgsConstructor
@Configuration
public class JsonObjectConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job jsonObjectJob() {
        return jobBuilderFactory.get("jsonObjectJob")
                .incrementer(new RunIdIncrementer())
                .start(jsonObjectStep())
                .build();
    }

    @Bean
    public Step jsonObjectStep() {
        return stepBuilderFactory.get("jsonObjectStep")
                .<MyCustomer3, MyCustomer3>chunk(10)
                .reader(jsonCustomerFileReader(null))
                .writer(jsonCustomerFileWriter())
                .build();
    }

    @Bean
    public ItemWriter jsonCustomerFileWriter() {
        return (items) -> items.forEach(System.out::println);
    }


    @Bean
    @StepScope
    public JsonItemReader<MyCustomer3> jsonCustomerFileReader(
            @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile
            ) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));

        JacksonJsonObjectReader<MyCustomer3> jsonObjectReader = new JacksonJsonObjectReader<>(MyCustomer3.class);
        jsonObjectReader.setMapper(objectMapper);

        return new JsonItemReaderBuilder<MyCustomer3>()
                .name("jsonCustomerFileReader")
                .jsonObjectReader(jsonObjectReader)
                .resource(inputFile)
                .build();
    }
}
