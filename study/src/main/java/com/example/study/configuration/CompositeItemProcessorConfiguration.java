package com.example.study.configuration;

import com.example.study.dto.MyCustomer2;
import com.example.study.service.UpperCaseNameService;
import com.example.study.validator.UniqueLastNameValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.adapter.ItemProcessorAdapter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.ScriptItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class CompositeItemProcessorConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final UpperCaseNameService service;

    @Bean
    public Job compositeItemProcessorJob() {
        return jobBuilderFactory.get("compositeItemProcessorJob")
                .start(compositeItemProcessorStep())
                .build();
    }

    @Bean
    public Step compositeItemProcessorStep() {
        return stepBuilderFactory.get("compositeItemProcessorStep")
                .<MyCustomer2, MyCustomer2>chunk(5)
                .reader(compositeItemProcessorReader(null))
                .processor(compositeItemProcessor())
                .writer(items -> {
                    items.forEach(System.out::println);
                })
                .build();
    }

    @Bean
    public UniqueLastNameValidator compositeItemProcessorUniqueLastNameValidator() {
        UniqueLastNameValidator uniqueLastNameValidator = new UniqueLastNameValidator();
        uniqueLastNameValidator.setName("validator");

        return uniqueLastNameValidator;
    }

    @Bean
    public ValidatingItemProcessor<MyCustomer2> compositeItemProcessorByValidate() {
        ValidatingItemProcessor<MyCustomer2> itemProcessor = new ValidatingItemProcessor<>(compositeItemProcessorUniqueLastNameValidator());
        itemProcessor.setFilter(true);
        return itemProcessor;
    }

    @Bean
    public ItemProcessorAdapter<MyCustomer2, MyCustomer2> compositeItemProcessorByAdapter() {
        ItemProcessorAdapter<MyCustomer2, MyCustomer2> adapter = new ItemProcessorAdapter<MyCustomer2, MyCustomer2>();
        adapter.setTargetObject(service);
        adapter.setTargetMethod("upperCase");
        return adapter;
    }


    @Bean
    @StepScope
    public ScriptItemProcessor<MyCustomer2, MyCustomer2> compositeItemProcessorByScript(
            @Value("#{jobParameters['script']}") ClassPathResource script
    ) {
        ScriptItemProcessor<MyCustomer2, MyCustomer2> itemProcessor = new ScriptItemProcessor<>();
        itemProcessor.setScript(script);
        return itemProcessor;
    }

    @Bean
    public CompositeItemProcessor<MyCustomer2, MyCustomer2> compositeItemProcessor() {
        CompositeItemProcessor<MyCustomer2, MyCustomer2> itemProcessor = new CompositeItemProcessor<>();

        itemProcessor.setDelegates(Arrays.asList(
                compositeItemProcessorByValidate(),
                compositeItemProcessorByAdapter(),
                compositeItemProcessorByScript(null)
        ));
        return itemProcessor;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<MyCustomer2> compositeItemProcessorReader(
            @Value("#{jobParameters['customerFile']}") FileSystemResource inputFile) {
        return new FlatFileItemReaderBuilder<MyCustomer2>()
                .name("compositeItemProcessorReader")
                .delimited().delimiter(",")
                .names("firstName", "middleInitial", "lastName",
                        "address" , "city" , "state", "zipCode")
                .targetType(MyCustomer2.class)
                .resource(inputFile)
                .build();
    }
}
