package com.example.study.configuration;

import com.example.study.dto.MyCustomer3;
import com.example.study.dto.Transaction2;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
@RequiredArgsConstructor
public class StaxConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public StaxEventItemReader customerFileStaxReader(
            @Value("#{jobParameters['customerFile']}") ClassPathResource inputFile) {
        return new StaxEventItemReaderBuilder<>()
                .name("customerFileStaxReader")
                .resource(inputFile)
                .addFragmentRootElements("customer")
                .unmarshaller(customerFileStaxMarshaller())
                .build();
    }

    @Bean
    public Jaxb2Marshaller customerFileStaxMarshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(MyCustomer3.class, Transaction2.class);
        return jaxb2Marshaller;
    }

    @Bean
    public Step customerFileStaxStep() {
        return stepBuilderFactory.get("copyFileStep")
                .<MyCustomer3, MyCustomer3>chunk(10)
                .reader(customerFileStaxReader(null))
                .writer(customerFileStaxWriter())
                .build();
    }

    @Bean
    public ItemWriter customerFileStaxWriter() {
        return (items) -> items.forEach(System.out::println);
    }

    @Bean
    public Job customerFileStaxJob() {
        return jobBuilderFactory.get("customerFileStaxJob")
                .incrementer(new RunIdIncrementer())
                .start(customerFileStaxStep())
                .build();
    }
}
