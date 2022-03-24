package com.example.study.configuration;

import com.example.study.dto.MyCustomer2;
import com.example.study.service.UpperCaseNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.adapter.ItemProcessorAdapter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.ScriptItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@RequiredArgsConstructor
@Configuration
public class ItemAdapterConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final UpperCaseNameService service;

    @Bean
    public Job itemAdapterJob() {
        return jobBuilderFactory.get("itemAdapterJob")
                .start(itemAdapterStep())
                .build();
    }

    @Bean
    public Step itemAdapterStep() {
        return stepBuilderFactory.get("itemAdapterStep")
                .<MyCustomer2, MyCustomer2>chunk(5)
                .reader(itemAdapterReader(null))
//                .processor(itemAdapterProcessor())
                .processor(itemScriptProcessor(null))
                .writer(items -> items.forEach(System.out::println))
                .build();
    }

    @Bean
    public ItemProcessorAdapter<MyCustomer2, MyCustomer2> itemAdapterProcessor() {
        ItemProcessorAdapter<MyCustomer2, MyCustomer2> adapter = new ItemProcessorAdapter<MyCustomer2, MyCustomer2>();
        adapter.setTargetObject(service);
        adapter.setTargetMethod("upperCase");
        return adapter;
    }

    @Bean
    @StepScope
    public ScriptItemProcessor<MyCustomer2, MyCustomer2> itemScriptProcessor(
            @Value("#{jobParameters['script']}") ClassPathResource script
    ) {
            ScriptItemProcessor<MyCustomer2, MyCustomer2> itemProcessor = new ScriptItemProcessor<>();
            itemProcessor.setScript(script);
            return itemProcessor;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<MyCustomer2> itemAdapterReader(
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
