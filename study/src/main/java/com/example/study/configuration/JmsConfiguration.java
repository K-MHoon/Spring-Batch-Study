package com.example.study.configuration;

import com.example.study.dto.Customer4;
import com.example.study.dto.WriteCustomer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.jms.JmsItemReader;
import org.springframework.batch.item.jms.JmsItemWriter;
import org.springframework.batch.item.jms.builder.JmsItemReaderBuilder;
import org.springframework.batch.item.jms.builder.JmsItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class JmsConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final JmsTemplate jmsTemplate;

    @Bean
    public Job jmsFormatJob() {
        return jobBuilderFactory.get("jmsFormatJob")
                .start(formatInputStep())
                .next(formatOutputStep())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<WriteCustomer> writeCustomerJmsItemReader(
            @Value("#{jobParameters['customerFile']}") FileSystemResource inputFile) {
        return new FlatFileItemReaderBuilder<WriteCustomer>()
                .name("writeCustomerJmsItemReader")
                .delimited()
                .names("firstName", "middleInitial", "lastName",
                        "address", "city", "state", "zipCode")
                .targetType(WriteCustomer.class)
                .resource(inputFile)
                .build();
    }

    @Bean
    public JmsItemReader<WriteCustomer> jmsItemReader() {
        return new JmsItemReaderBuilder<WriteCustomer>()
                .jmsTemplate(jmsTemplate)
                .itemType(WriteCustomer.class)
                .build();
    }

    @Bean
    public JmsItemWriter<WriteCustomer> jmsItemWriter() {
        return new JmsItemWriterBuilder<WriteCustomer>()
                .jmsTemplate(jmsTemplate)
                .build();
    }

    @Bean
    public Step formatInputStep() {
        return stepBuilderFactory.get("formatInputStep")
                .<WriteCustomer, WriteCustomer>chunk(10)
                .reader(writeCustomerJmsItemReader(null))
                .writer(jmsItemWriter())
                .build();
    }

    @Bean
    public Step formatOutputStep() {
        return stepBuilderFactory.get("formatInputStep")
                .<WriteCustomer, WriteCustomer>chunk(10)
                .reader(jmsItemReader())
                .writer(jmsXmlWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public StaxEventItemWriter<? super WriteCustomer> jmsXmlWriter(
            @Value("#{jobParameters['outputFile']}") FileSystemResource outputFile) {

        Map<String, Class<?>> aliases = new HashMap<>();
        aliases.put("customer", WriteCustomer.class);

        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(aliases);

        return new StaxEventItemWriterBuilder<WriteCustomer>()
                .name("jmsXmlWriter")
                .marshaller(marshaller)
                .resource(outputFile)
                .rootTagName("customer")
                .build();
    }
}
