package com.example.study.configuration;

import com.example.study.dto.MyCustomer5;
import com.example.study.repository.MyCustomerRepository;
import com.example.study.service.MyCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.Collections;

@Configuration
@RequiredArgsConstructor
public class AdapterConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final MyCustomerService customerService;

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
    public ItemReaderAdapter<MyCustomer5> adapterItemReader() {
        ItemReaderAdapter<MyCustomer5> adapter = new ItemReaderAdapter<>();

        adapter.setTargetObject(customerService);
        adapter.setTargetMethod("getCustomer");

        return adapter;
    }
}
