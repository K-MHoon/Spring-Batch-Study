package com.example.study.configuration;

import com.example.study.dto.Customer4;
import com.example.study.listener.MultiThreadCustomItemProcessListener;
import com.example.study.listener.MultiThreadCustomItemReadListener;
import com.example.study.listener.MultiThreadCustomItemWriteListener;
import com.example.study.listener.StopWatchJobListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class MultiThreadStepConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    private final DataSource dataSource;
    private final int chunkSize = 2;

    @Bean
    public Job multiThreadJob() throws Exception {
        return jobBuilderFactory.get("multiThreadJob")
                .incrementer(new RunIdIncrementer())
                .start(multiThreadStep1())
                .listener(new StopWatchJobListener())
                .build();
    }

    @Bean
    public Step multiThreadStep1() throws Exception {
        return stepBuilderFactory.get("multiThreadStep1")
                .<Customer4, Customer4>chunk(chunkSize)
                .reader(multiThreadPagingItemReader())
                .listener(new MultiThreadCustomItemReadListener())
                .processor((ItemProcessor<Customer4,Customer4>) item -> item)
                .listener(new MultiThreadCustomItemProcessListener())
                .writer(multiThreadItemWriter())
                .listener(new MultiThreadCustomItemWriteListener())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setMaxPoolSize(8);
        taskExecutor.setThreadNamePrefix("async-thread");

        return taskExecutor;
    }


    @Bean
    public ItemReader<Customer4> multiThreadPagingItemReader() throws Exception {

        return new JdbcPagingItemReaderBuilder<Customer4>()
                .name("multiThreadPagingItemReader")
                .pageSize(chunkSize)
                .beanRowMapper(Customer4.class)
                .dataSource(dataSource)
                .queryProvider(multiThreadCreateQueryProvider())
                .build();
    }

    @Bean
    public ItemWriter<? super Customer4> multiThreadItemWriter() {
        return new JdbcBatchItemWriterBuilder<Customer4>()
                .dataSource(dataSource)
                .sql("insert into customer2 values (:id, :firstName, :lastName, :birthdate)")
                .beanMapped()
                .build();
    }

    @Bean
    public PagingQueryProvider multiThreadCreateQueryProvider() throws Exception {

        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause("id, first_name, last_name, birthdate");
        factoryBean.setFromClause("from customer");

        Map<String, Order> sortKey = new HashMap<>();
        sortKey.put("id", Order.ASCENDING);

        factoryBean.setSortKeys(sortKey);

        return factoryBean.getObject();
    }
}
