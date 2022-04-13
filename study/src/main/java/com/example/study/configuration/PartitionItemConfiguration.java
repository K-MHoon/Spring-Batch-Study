package com.example.study.configuration;

import com.example.study.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class PartitionItemConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    /**
     *
     * @param resource
     * @return
     */
    @Bean
    @StepScope
    public FlatFileItemReader<Transaction> partitionItemFileReader(
            @Value("#{stepExecutionContext['file']}") FileSystemResource resource) {
        return new FlatFileItemReaderBuilder<Transaction>()
                .name("partitionItemFileReader")
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
    public MultiResourcePartitioner multiResourcePartitionerByTransaction(
            @Value("#{jobParameters['inputFiles']}") Resource[] resources
    ) {

        MultiResourcePartitioner multiResourcePartitioner = new MultiResourcePartitioner();
        multiResourcePartitioner.setKeyName("file");
        multiResourcePartitioner.setResources(resources);
        return multiResourcePartitioner;
    }

    @Bean
    public TaskExecutorPartitionHandler taskExecutorPartitionHandler() {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setStep(partitionItemStep1());
        taskExecutorPartitionHandler.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return taskExecutorPartitionHandler;
    }

    @Bean
    public Step partitionedMaster() {
        return stepBuilderFactory.get("partitionedMaster")
                .partitioner(partitionItemStep1().getName(), multiResourcePartitionerByTransaction(null))
                .partitionHandler(taskExecutorPartitionHandler())
                .build();
    }

    @Bean
    public Step partitionItemStep1() {
        return stepBuilderFactory.get("partitionItemStep1")
                .<Transaction, Transaction>chunk(100)
                .reader(partitionItemFileReader(null))
                .writer(partitionItemWriter(null))
                .build();
    }

    @Bean
    public Job partitionedJob() {
        return jobBuilderFactory.get("partitionedJob")
                .incrementer(new RunIdIncrementer())
                .start(partitionedMaster())
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<Transaction> partitionItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("insert into tbl_transaction (account_number, amount, timestamp) " +
                        "values (:accountNumber, :amount, :timestamp)")
                .build();
    }

}
