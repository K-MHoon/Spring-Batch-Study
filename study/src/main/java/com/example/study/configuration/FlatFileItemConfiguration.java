package com.example.study.configuration;

import com.example.study.dto.MyCustomer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

@Configuration
@RequiredArgsConstructor
public class FlatFileItemConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job flatFileItemJob() {
        return jobBuilderFactory.get("flatFileItemJob")
                .start(copyFileStep())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<MyCustomer> customerFlatFileItemReader(
            @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {
        return new FlatFileItemReaderBuilder<MyCustomer>()
                .name("customerFlatFileItemReader")
                .resource(inputFile)
                .fixedLength()
                .addColumns(new Range(1, 11))
                .addColumns(new Range(12, 12))
                .addColumns(new Range(13, 22))
                .addColumns(new Range(23, 26))
                .addColumns(new Range(27, 46))
                .addColumns(new Range(47, 62))
                .addColumns(new Range(63, 64))
                .addColumns(new Range(65, 69))
                .names("firstName", "middleInitial", "lastName",
                        "addressNumber" , "street" ,
                        "city" , "state", "zipCode")
                .targetType(MyCustomer.class)
                .build();

    }

    @Bean
    public ItemWriter<MyCustomer> customerFlatFileItemWriter() {
        return (items) -> items.forEach(System.out::println);
    }

    @Bean
    public Step copyFileStep() {
        return stepBuilderFactory.get("copyFileStep")
                .<MyCustomer, MyCustomer>chunk(10)
                .reader(customerFlatFileItemReader(null))
                .writer(customerFlatFileItemWriter())
                .build();
    }
}
