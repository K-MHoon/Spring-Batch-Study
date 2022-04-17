package com.example.partition.configuration;

import com.example.partition.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilderFactory;
import org.springframework.batch.integration.config.annotation.EnableBatchIntegration;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

@Configuration
@EnableBatchIntegration
public class BatchConfiguration {


    @Configuration
    @Profile("!worker")
    @RequiredArgsConstructor
    public static class ManagerConfiguration {

        private final JobBuilderFactory jobBuilderFactory;
        private final RemoteChunkingManagerStepBuilderFactory remoteChunkingManagerStepBuilderFactory;

        @Bean
        public DirectChannel requests() {
            return new DirectChannel();
        }

        @Bean
        public IntegrationFlow outboundFlow(AmqpTemplate amqpTemplate) {
            return IntegrationFlows.from(requests())
                    .handle(Amqp.outboundAdapter(amqpTemplate).routingKey("requests"))
                    .get();
        }

        @Bean
        public QueueChannel replies() {
            return new QueueChannel();
        }

        @Bean
        public IntegrationFlow inboundFlow(ConnectionFactory connectionFactory) {
            return IntegrationFlows
                    .from(Amqp.inboundAdapter(connectionFactory, "replies"))
                    .channel(replies())
                    .get();
        }

        @Bean
        @StepScope
        public FlatFileItemReader<Transaction> fileTransactionReader(
                @Value("#{jobParameters['inputFlatFile']}") FileSystemResource resource) {
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
        public TaskletStep masterStep() {
            return remoteChunkingManagerStepBuilderFactory.get("masterStep")
                    .<Transaction, Transaction>chunk(1)
                    .reader(fileTransactionReader(null))
                    .outputChannel(requests())
                    .inputChannel(replies())
                    .build();
        }

        @Bean
        public Job remoteChunkingJob() {
            return jobBuilderFactory.get("remoteChunkingJob")
                    .start(masterStep())
                    .build();
        }
    }
}
