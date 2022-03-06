package com.example.study.configuration;

import com.example.study.service.CustomService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ItemReaderAdapterConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job itemReaderAdapterJob() {
        return jobBuilderFactory.get("itemReaderAdapterJob")
                .incrementer(new RunIdIncrementer())
                .start(itemReaderAdapterStep1())
                .build();
    }

    @Bean
    public Step itemReaderAdapterStep1() {
        return stepBuilderFactory.get("itemReaderAdapterStep1")
                .<String, String>chunk(10)
                .reader(itemReaderAdapterReader())
                .writer(itemReaderAdapterWriter())
                .build();
    }

    @Bean
    public ItemReader<String> itemReaderAdapterReader() {
        ItemReaderAdapter<String> reader = new ItemReaderAdapter<>();
        reader.setTargetObject(itemReaderAdapterService());
        reader.setTargetMethod("customRead");
        return reader;
    }

    @Bean
    public Object itemReaderAdapterService() {
        return new CustomService();
    }

    @Bean
    public ItemWriter<? super String> itemReaderAdapterWriter() {
        return items -> {
            for (String item : items) {
                System.out.println(item.toString());
            }
        };
    }


}
