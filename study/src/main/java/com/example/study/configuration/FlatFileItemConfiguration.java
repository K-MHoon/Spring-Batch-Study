package com.example.study.configuration;

import com.example.study.dto.MyCustomer;
import com.example.study.dto.MyCustomer2;
import com.example.study.tokenizer.CustomFlatFileLineTokenizer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;

@Configuration
@RequiredArgsConstructor
public class FlatFileItemConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job flatFileItemJob() {
        return jobBuilderFactory.get("flatFileItemJob")
                .incrementer(new RunIdIncrementer())
//                .start(copyFileStep())
                .start(copyFileStep2())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<MyCustomer> customerFlatFileItemReaderByFixedLength(
            @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {
        return new FlatFileItemReaderBuilder<MyCustomer>()
                .name("customerFlatFileItemReaderByFixedLength")
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
    @StepScope
    public FlatFileItemReader<MyCustomer> customerFlatFileItemReaderByDelimit(
            @Value("#{jobParameters['customerFile']}") FileSystemResource inputFile) {
        return new FlatFileItemReaderBuilder<MyCustomer>()
                .name("customerFlatFileItemReaderByDelimit")
                .delimited().delimiter(",")
                .names("firstName", "middleInitial", "lastName",
                        "addressNumber" , "street" ,
                        "city" , "state", "zipCode")
                .targetType(MyCustomer.class)
                .resource(inputFile)
                .build();
    }
    @Bean
    @StepScope
    public FlatFileItemReader<MyCustomer2> customerFlatFileItemReaderByDelimitAndCustom(
            @Value("#{jobParameters['customerFile']}") FileSystemResource inputFile) {
        return new FlatFileItemReaderBuilder<MyCustomer2>()
                .name("customerFlatFileItemReaderByDelimitAndCustom")
                .lineTokenizer(new CustomFlatFileLineTokenizer())
                .targetType(MyCustomer2.class)
                .resource(inputFile)
                .build();
    }

    @Bean
    public ItemWriter<MyCustomer> customerFlatFileItemWriter() {
        return (items) -> items.forEach(System.out::println);
    }

    @Bean
    public ItemWriter<MyCustomer2> customerFlatFileItemWriter2() {
        return (items) -> items.forEach(System.out::println);
    }

    @Bean
    public Step copyFileStep() {
        return stepBuilderFactory.get("copyFileStep")
                .<MyCustomer, MyCustomer>chunk(10)
//                .reader(customerFlatFileItemReaderByFixedLength(null))
                .reader(customerFlatFileItemReaderByDelimit(null))
                .writer(customerFlatFileItemWriter())
                .build();
    }

    @Bean
    public Step copyFileStep2() {
        return stepBuilderFactory.get("copyFileStep2")
                .<MyCustomer2, MyCustomer2>chunk(10)
//                .reader(customerFlatFileItemReaderByFixedLength(null))
                .reader(customerFlatFileItemReaderByDelimitAndCustom(null))
                .writer(customerFlatFileItemWriter2())
                .build();
    }
}
