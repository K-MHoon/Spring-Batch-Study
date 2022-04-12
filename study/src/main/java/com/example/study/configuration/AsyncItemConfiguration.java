package com.example.study.configuration;

import com.example.study.entity.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.sql.DataSource;
import java.util.concurrent.Future;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AsyncItemConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public AsyncItemProcessor<Transaction, Transaction> transactionAsyncItemProcessor(){
        AsyncItemProcessor<Transaction, Transaction> processor = new AsyncItemProcessor<>();

        processor.setDelegate(transactionItemProcessorByDelegate());
        processor.setTaskExecutor(new SimpleAsyncTaskExecutor());

        return processor;
    }

    @Bean
    public ItemProcessor<Transaction, Transaction> transactionItemProcessorByDelegate() {
        return (transaction) -> {
            log.info("Hello! Async Item Processor");
            Thread.sleep(5);
            return transaction;
        };
    }

    @Bean
    public AsyncItemWriter<Transaction> transactionAsyncItemWriter() {
        AsyncItemWriter<Transaction> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(transactionJdbcBatchItemWriterByDelegate(null));
        return asyncItemWriter;
    }

    @Bean
    public Step transactionAsyncStep1() {
        return this.stepBuilderFactory.get("transactionAsyncStep1")
                .<Transaction, Future<Transaction>>chunk(1)
                .reader(transactionFileTransactionReader(null))
                .processor(transactionAsyncItemProcessor())
                .writer(transactionAsyncItemWriter())
                .build();
    }

    @Bean
    public Job transactionAsyncJob() {
        return jobBuilderFactory.get("transactionAsyncJob")
                .incrementer(new RunIdIncrementer())
                .start(transactionAsyncStep1())
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Transaction> transactionFileTransactionReader(
            @Value("#{jobParameters['inputFlatFile']}") FileSystemResource resource) {
        return new FlatFileItemReaderBuilder<Transaction>()
                .name("transactionFileTransactionReader")
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
    public JdbcBatchItemWriter<Transaction> transactionJdbcBatchItemWriterByDelegate(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("insert into tbl_transaction (account_number, amount, timestamp) " +
                        "values (:accountNumber, :amount, :timestamp)")
                .build();
    }



}
