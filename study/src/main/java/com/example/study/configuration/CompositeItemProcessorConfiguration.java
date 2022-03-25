package com.example.study.configuration;

import com.example.study.classifier.ZipCodeClassifier;
import com.example.study.dto.MyCustomer2;
import com.example.study.service.LowerCaseNameService;
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
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.ScriptItemProcessor;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.classify.Classifier;
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
    private final UpperCaseNameService upperService;
    private final LowerCaseNameService lowerService;

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
                .processor(classifierCompositeItemProcessor())
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
    public ItemProcessorAdapter<MyCustomer2, MyCustomer2> compositeItemProcessorByAdapterUpper() {
        ItemProcessorAdapter<MyCustomer2, MyCustomer2> adapter = new ItemProcessorAdapter<MyCustomer2, MyCustomer2>();
        adapter.setTargetObject(upperService);
        adapter.setTargetMethod("upperCase");
        return adapter;
    }

    @Bean
    public ItemProcessorAdapter<MyCustomer2, MyCustomer2> compositeItemProcessorByAdapterLower() {
        ItemProcessorAdapter<MyCustomer2, MyCustomer2> adapter = new ItemProcessorAdapter<MyCustomer2, MyCustomer2>();
        adapter.setTargetObject(lowerService);
        adapter.setTargetMethod("lowerCase");
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
                compositeItemProcessorByAdapterUpper(),
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

    @Bean
    public Classifier zipCodeClassifierItemProcessor() {
        return new ZipCodeClassifier(compositeItemProcessorByAdapterUpper(),
                compositeItemProcessorByAdapterLower());
    }

    @Bean
    public ClassifierCompositeItemProcessor<MyCustomer2, MyCustomer2> classifierCompositeItemProcessor() {
        ClassifierCompositeItemProcessor<MyCustomer2, MyCustomer2> itemProcessor =
                new ClassifierCompositeItemProcessor<>();
        itemProcessor.setClassifier(zipCodeClassifierItemProcessor());
        return itemProcessor;
    }
}
