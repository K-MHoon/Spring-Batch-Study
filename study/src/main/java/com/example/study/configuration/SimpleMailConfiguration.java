package com.example.study.configuration;

import com.example.study.dto.EmailCustomer;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.mail.SimpleMailMessageItemWriter;
import org.springframework.batch.item.mail.builder.SimpleMailMessageItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class SimpleMailConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    @StepScope
    public FlatFileItemReader<EmailCustomer> emailCustomerFlatFileItemReader(
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
    public JdbcBatchItemWriter<EmailCustomer> emailCustomerJdbcBatchItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<EmailCustomer>()
                .namedParametersJdbcTemplate(new NamedParameterJdbcTemplate(dataSource))
                .sql("INSERT INTO EMAIL_CUSTOMER (first_name, middle_initial, last_name, " +
                        "address, city, state, zip, email) " +
                        "VALUES(:firstName, :middleInitial, :lastName, :address, :city, :state, :zip, :email)")
                .beanMapped()
                .build();
    }

    @Bean
    public JdbcCursorItemReader<EmailCustomer> emailCustomerJdbcCursorItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<EmailCustomer>()
                .name("emailCustomerJdbcCursorItemReader")
                .dataSource(dataSource)
                .sql("select * from email_customer")
                .beanRowMapper(EmailCustomer.class)
                .build();
    }

    @Bean
    public SimpleMailMessageItemWriter simpleMailMessageItemWriter(MailSender mailSender) {
        return new SimpleMailMessageItemWriterBuilder()
                .mailSender(mailSender)
                .build();
    }

    @Bean
    public Step emailCustomerImportStep() {
        return stepBuilderFactory.get("emailCustomerImportStep")
                .<EmailCustomer, EmailCustomer>chunk(10)
                .reader(emailCustomerFlatFileItemReader(null))
                .writer(emailCustomerJdbcBatchItemWriter(null))
                .build();
    }

    @Bean
    public Step emailCustomerEmailStep() {
        return stepBuilderFactory.get("emailCustomerEmailStep")
                .<EmailCustomer, SimpleMailMessage>chunk(10)
                .reader(emailCustomerFlatFileItemReader(null))
                .processor((ItemProcessor<EmailCustomer, SimpleMailMessage>) item -> {
                    SimpleMailMessage mail = new SimpleMailMessage();
                    mail.setFrom("prospringbatch@gmail.com");
                    mail.setTo(item.getEmail());
                    mail.setSubject("Welcome!");
                    mail.setText(String.format("Welcome %s, %s,\n" +
                            "You were " +
                            "imported into the system using Spring Batch!",
                            item.getFirstName(), item.getLastName()));
                    return mail;
                })
                .writer(simpleMailMessageItemWriter(null))
                .build();
    }

    @Bean
    public Job emailJob() {
        return jobBuilderFactory.get("emailJob")
                .start(emailCustomerImportStep())
                .next(emailCustomerEmailStep())
                .build();
    }
}
