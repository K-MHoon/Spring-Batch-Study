package com.example.study.configuration;

import com.example.study.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.deployer.spi.task.TaskLauncher;
import org.springframework.cloud.task.batch.partition.DeployerPartitionHandler;
import org.springframework.cloud.task.batch.partition.DeployerStepExecutionHandler;
import org.springframework.cloud.task.batch.partition.PassThroughCommandLineArgsProvider;
import org.springframework.cloud.task.batch.partition.SimpleEnvironmentVariablesProvider;
import org.springframework.cloud.task.repository.TaskRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DeployPartitionConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobRepository jobRepository;
    private final ConfigurableApplicationContext context;

    @Bean
    @Profile("master2")
    public DeployerPartitionHandler deployerPartitionHandler(TaskLauncher taskLauncher,
                                                     JobExplorer jobExplorer,
                                                     ApplicationContext context,
                                                     Environment environment,
                                                     TaskRepository taskRepository) {
        Resource resource = context.getResource("file:///target/study-0.0.1-SNAPSHOT.jar");
        DeployerPartitionHandler partitionHandler = new DeployerPartitionHandler(taskLauncher, jobExplorer, resource, "deployItemStep", taskRepository);

        List<String> commandLineArgs = new ArrayList<>();
        commandLineArgs.add("--spring.profiles.active=worker2");
        commandLineArgs.add("--spring.cloud.task.initialize.enable=false");
        commandLineArgs.add("--spring.batch.initializer.enabled=false");
        commandLineArgs.add("--spring.datasource.initialize=false");
        partitionHandler.setCommandLineArgsProvider(
                new PassThroughCommandLineArgsProvider(commandLineArgs));
        partitionHandler.setEnvironmentVariablesProvider(
                new SimpleEnvironmentVariablesProvider(environment));
        partitionHandler.setMaxWorkers(3);
        partitionHandler.setApplicationName("PartitionedBatchJobTask");
        return partitionHandler;
    }

    @Bean
    @Profile("worker2")
    public DeployerStepExecutionHandler deployerWorkerStepExecutionHandler(JobExplorer jobExplorer) {
        return new DeployerStepExecutionHandler(this.context, jobExplorer, this.jobRepository);
    }


    @Bean
    public Step deployItemStep() {
        return stepBuilderFactory.get("deployItemStep")
                .<Transaction, Transaction>chunk(1)
                .reader(deployItemFileReader(null))
                .writer(deployItemWriter(null))
                .build();
    }

    /**
     *
     * @param resource
     * @return
     */
    @Bean
    @StepScope
    public FlatFileItemReader<Transaction> deployItemFileReader(
            @Value("#{jobParameters['file']}") FileSystemResource resource) {
        return new FlatFileItemReaderBuilder<Transaction>()
                .name("deployItemFileReader")
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
    public JdbcBatchItemWriter<Transaction> deployItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .dataSource(dataSource)
                .beanMapped()
                .sql("insert into tbl_transaction (account_number, amount, timestamp) " +
                        "values (:accountNumber, :amount, :timestamp)")
                .build();
    }

    @Bean
    public Job deployItemJob() {
        return jobBuilderFactory.get("deployItemJob")
                .start(deployItemStep())
                .build();
    }
}
