package com.example.study.configuration;

import com.example.study.entity.AccountSummary;
import com.example.study.entity.Transaction;
import com.example.study.processor.TransactionItemProcessor;
import com.example.study.reader.TransactionItemReader;
import com.example.study.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class TransactionConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final TransactionRepository transactionRepository;

    @Bean
    @StepScope
    public TransactionItemReader transactionItemReader() {
        return new TransactionItemReader(fileItemReader(null));
    }

    @Bean
    @StepScope
    public FlatFileItemReader<FieldSet> fileItemReader(@Value("#{jobParameters['transactionFile']}") Resource inputFile) {
        return new FlatFileItemReaderBuilder<FieldSet>()
                .name("flatItemReader")
                .resource(inputFile)
                .lineTokenizer(new DelimitedLineTokenizer())
                .fieldSetMapper(new PassThroughFieldSetMapper())
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Transaction> transactionItemWriter() {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("insert into tbl_transaction " +
                        "(account_summary_id, timestamp, amount) " +
                        "values ((select id from tbl_account_summary " +
                        " where account_number = :accountNumber), " +
                        ":timestamp, :amount)")
                .build();
    }

    @Bean
    @JobScope
    public Step importTransactionFileStep() {
        return stepBuilderFactory.get("importTransactionFileStep")
                .<Transaction, Transaction>chunk(100)
                .reader(transactionItemReader())
                .writer(transactionItemWriter())
                .allowStartIfComplete(true)
                .listener(transactionItemReader())
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<AccountSummary> accountSummaryItemReader() {
        return new JdbcCursorItemReaderBuilder<AccountSummary>()
                .name("accountSummaryItemReader")
                .dataSource(dataSource)
                .beanRowMapper(AccountSummary.class)
                .build();
    }

    @Bean
    @StepScope
    public TransactionItemProcessor transactionItemProcessor() {
        return new TransactionItemProcessor(transactionRepository);
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<AccountSummary> accountSummaryWriter() {
        return new JdbcBatchItemWriterBuilder<AccountSummary>()
                .dataSource(dataSource)
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("update tbl_account_summary " +
                        "set current_balance = :currentBalance " +
                        "where account_number = :accountNumber")
                .build();
    }

    @Bean
    @JobScope
    public Step applyTransactionStep() {
        return stepBuilderFactory.get("applyTransactionStep")
                .<AccountSummary, AccountSummary>chunk(100)
                .reader(accountSummaryItemReader())
                .processor(transactionItemProcessor())
                .writer(accountSummaryWriter())
                .build();
    }
}
