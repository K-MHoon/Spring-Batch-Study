package com.example.study.configuration;

import com.example.study.dto.MyCustomer;
import com.example.study.dto.MyCustomer2;
import com.example.study.validator.UniqueLastNameValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.validator.BeanValidatingItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
@RequiredArgsConstructor
public class ValidationProcessorConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job validationJob() {
        return jobBuilderFactory.get("validationJob")
//                .incrementer(new RunIdIncrementer())
                .start(validationStep())
                .build();
    }

    @Bean
    public Step validationStep() {
        return stepBuilderFactory.get("validationStep")
                .<MyCustomer2, MyCustomer2>chunk(5)
                .reader(validationItemReader(null))
                .processor(validationItemProcessor())
                .writer(items -> {
                    items.forEach(System.out::println);
                })
                .stream(uniqueLastNameValidator())
                .build();
    }

//    @Bean
//    public BeanValidatingItemProcessor<MyCustomer2> validationItemProcessor() {
//        return new BeanValidatingItemProcessor<>();
//    }

    @Bean
    public UniqueLastNameValidator uniqueLastNameValidator() {
        UniqueLastNameValidator uniqueLastNameValidator = new UniqueLastNameValidator();
        uniqueLastNameValidator.setName("validator");
        return uniqueLastNameValidator;
    }

    @Bean
    public ValidatingItemProcessor<MyCustomer2> validationItemProcessor() {
        return new ValidatingItemProcessor<>(uniqueLastNameValidator());
    }

    @Bean
    @StepScope
    public FlatFileItemReader<MyCustomer2> validationItemReader(
            @Value("#{jobParameters['customerFile']}") FileSystemResource inputFile) {
        return new FlatFileItemReaderBuilder<MyCustomer2>()
                .name("validationItemReader")
                .delimited().delimiter(",")
                .names("firstName", "middleInitial", "lastName",
                        "address" , "city" , "state", "zipCode")
                .targetType(MyCustomer2.class)
                .resource(inputFile)
                .build();
    }
}
