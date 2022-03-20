package com.example.study.configuration;

import com.example.study.dto.MyCustomer5;
import com.example.study.service.MyCustomerItemReader;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AdapterConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job adapterJob() {
        return jobBuilderFactory.get("adapterJob")
                .incrementer(new RunIdIncrementer())
                .start(adapterStep())
                .build();
    }

    @Bean
    public Step adapterStep() {
        return stepBuilderFactory.get("adapterStep")
                .<MyCustomer5, MyCustomer5>chunk(10)
                .reader(adapterItemReader())
                .writer((items) -> items.forEach(System.out::println))
                .build();
    }

    @Bean
    public ItemReader adapterItemReader() {
        MyCustomerItemReader myCustomerItemReader = new MyCustomerItemReader();
        myCustomerItemReader.setName("adapterItemReader");
        return myCustomerItemReader;
    }
}
