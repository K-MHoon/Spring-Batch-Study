package com.example.study.configuration;

import com.example.study.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class ParallelStepsConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job parallelStepsJob() {
        Flow secondFlow = new FlowBuilder<Flow>("secondFlow")
                .start(parallelConfigurationStep2())
                .build();

        Flow parallelFlow = new FlowBuilder<Flow>("parallelFlow")
                .start(parallelConfigurationStep1())
                .split(new SimpleAsyncTaskExecutor())
                .add(secondFlow)
                .build();

        return jobBuilderFactory.get("parallelStepsJob")
                .start(parallelFlow)
                .end()
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Transaction> fileTransactionReader(
            @Value("#{jobParameters['inputFlatFile']}")FileSystemResource resource) {
        return new FlatFileItemReaderBuilder<Transaction>()
                .name("fileTransactionReader")
                .resource(resource)
                .delimited()
                .names("accountNumber", "amount", "timestamp")
                .fieldSetMapper(fieldSet -> {
                    Transaction transaction = new Transaction();

                    transaction.setAccountNumber(fieldSet.readString("accountNumber"));
                    transaction.setAmount(fieldSet.readDouble("amount"));
                    transaction.setTimestamp(fieldSet.readDate("timestamp", "yyyy-MM-dd HH:mm:ss"));
                    return transaction;
                })
                .build();
    }

    @Bean
    @StepScope
    public StaxEventItemReader<Transaction> xmlTransactionReader(
            @Value("#{jobParameters['inputXmlFile']}") FileSystemResource resource
    ) {
        Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
        unmarshaller.setClassesToBeBound(Transaction.class);

        return new StaxEventItemReaderBuilder<Transaction>()
                .name("xmlTransactionReader")
                .resource(resource)
                .addFragmentRootElements("transaction")
                .unmarshaller(unmarshaller)
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Transaction> parallelStepsItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("insert into tbl_transaction (account_number, amount, timestamp) " +
                        "values (:accountNumber, :amount, :timestamp)")
                .build();
    }

    @Bean
    public Step parallelConfigurationStep1() {
        return stepBuilderFactory.get("parallelConfigurationStep1")
                .<Transaction, Transaction>chunk(100)
                .reader(xmlTransactionReader(null))
                .writer(parallelStepsItemWriter(null))
                .build();
    }

    @Bean
    public Step parallelConfigurationStep2() {
        return stepBuilderFactory.get("parallelConfigurationStep2")
                .<Transaction, Transaction>chunk(100)
                .reader(fileTransactionReader(null))
                .writer(parallelStepsItemWriter(null))
                .build();
    }

}
