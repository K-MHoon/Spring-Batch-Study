package com.example.study.configuration;

import com.example.study.entity.Transaction;
import com.example.study.reader.TransactionItemReader;
import lombok.RequiredArgsConstructor;
import org.quartz.JobBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
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
    public Step importTransactionFileStep() {
        return stepBuilderFactory.get("importTransactionFileStep")
                .<Transaction, Transaction>chunk(100)
                .reader(transactionItemReader())
                .writer(transactionItemWriter())
                .allowStartIfComplete(true)
                .listener(transactionItemReader())
                .build();
    }
}
