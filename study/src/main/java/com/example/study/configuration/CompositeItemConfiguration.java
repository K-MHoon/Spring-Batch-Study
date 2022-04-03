package com.example.study.configuration;

import com.example.study.dto.EmailCustomer;
import com.example.study.dto.WriteCustomer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CompositeItemConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;


    @Bean
    public Job compositeWriterJob() {
        return jobBuilderFactory.get("compositeWriterJob")
                .incrementer(new RunIdIncrementer())
                .start(compositeWriterStep())
                .build();
    }

    @Bean
    public Step compositeWriterStep() {
        return stepBuilderFactory.get("compositeWriterStep")
                .<EmailCustomer, EmailCustomer>chunk(10)
                .reader(compositeFlatFileItemReader(null))
                .writer(compositeItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<EmailCustomer> compositeFlatFileItemReader(
            @Value("#{jobParameters['customerFile']}") FileSystemResource inputFile
    ) {
        return new FlatFileItemReaderBuilder<EmailCustomer>()
                .name("emailCustomerFlatFileItemReader")
                .resource(inputFile)
                .delimited()
                .names("firstName", "middleInitial", "lastName",
                        "address", "city", "state", "zip", "email")
                .targetType(EmailCustomer.class)
                .build();
    }

    @Bean
    public CompositeItemWriter<EmailCustomer> compositeItemWriter() {
        return new CompositeItemWriterBuilder<EmailCustomer>()
                .delegates(Arrays.asList(compositeStaxFileItemWriter(null),
                        compositeJdbcBatchItemWriter(null)))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<EmailCustomer> compositeJdbcBatchItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<EmailCustomer>()
                .namedParametersJdbcTemplate(new NamedParameterJdbcTemplate(dataSource))
                .sql("INSERT INTO EMAIL_CUSTOMER (first_name, middle_initial, last_name, " +
                        "address, city, state, zip, email) " +
                        "VALUES(:firstName, :middleInitial, :lastName, :address, :city, :state, :zip, :email)")
                .beanMapped()
                .build();
    }

    @Bean
    @StepScope
    public StaxEventItemWriter<EmailCustomer> compositeStaxFileItemWriter(
            @Value("#{jobParameters['outputFile']}") FileSystemResource outputFile) {

        Map<String, Class> aliases = new HashMap<>();
        aliases.put("customer", EmailCustomer.class);

        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(aliases);
        marshaller.afterPropertiesSet();

        return new StaxEventItemWriterBuilder<EmailCustomer>()
                .name("compositeStaxFileItemWriter")
                .resource(outputFile)
                .marshaller(marshaller)
                .rootTagName("customers")
                .build();
    }
}
