package com.example.springbatch.configuration;

import com.example.springbatch.dto.Customer4;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;

import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class SynchronizedConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final int chunkSize = 5;

    @Bean
    public Job synchronizedJob() {
        return jobBuilderFactory.get("synchronizedJob")
                .incrementer(new RunIdIncrementer())
                .start(synchronizedStep1())
                .build();
    }

    @Bean
    public Step synchronizedStep1() {
        return stepBuilderFactory.get("synchronizedStep1")
                .<Customer4, Customer4>chunk(chunkSize)
                .reader(synchronizedJdbcCursorItemReader())
                .listener(new ItemReadListener<Customer4>() {
                    @Override
                    public void beforeRead() {

                    }

                    @Override
                    public void afterRead(Customer4 item) {
                        System.out.println("Thread : " + Thread.currentThread().getName() + " item.getId() : " + item.getId());
                    }

                    @Override
                    public void onReadError(Exception ex) {

                    }
                })
                .writer(synchronizedItemWriter())
                .taskExecutor(synchronizedTaskExecutor())
                .build();
    }

    @Bean
    @StepScope
    public SynchronizedItemStreamReader<Customer4> synchronizedJdbcCursorItemReader() {

        JdbcCursorItemReader<Customer4> reader = new JdbcCursorItemReaderBuilder<Customer4>()
                .name("synchronizedJdbcCursorItemReader")
                .fetchSize(chunkSize)
                .sql("select id, first_name, last_name, birthdate from customer")
                .beanRowMapper(Customer4.class)
                .dataSource(dataSource)
                .build();

        return new SynchronizedItemStreamReaderBuilder<Customer4>()
                .delegate(reader)
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<? super Customer4> synchronizedItemWriter() {
        return new JdbcBatchItemWriterBuilder<Customer4>()
                .dataSource(dataSource)
                .sql("insert into customer2 values (:id, :firstName, :lastName, :birthdate)")
                .beanMapped()
                .build();
    }

    @Bean
    public TaskExecutor synchronizedTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setMaxPoolSize(8);
        taskExecutor.setThreadNamePrefix("async-thread");

        return taskExecutor;
    }
}
