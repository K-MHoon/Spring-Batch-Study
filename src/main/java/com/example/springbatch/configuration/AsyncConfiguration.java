package com.example.springbatch.configuration;

import com.example.springbatch.dto.Customer4;
import com.example.springbatch.listener.StopWatchJobListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class AsyncConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final int chunkSize = 100;

    @Bean
    public Job asyncJob() throws Exception {
        return jobBuilderFactory.get("asyncJob")
                .incrementer(new RunIdIncrementer())
                .start(asyncStep1())
                .listener(new StopWatchJobListener())
                .build();
    }

    @Bean
    public Step syncStep1() throws Exception {
        return stepBuilderFactory.get("syncStep1")
                .<Customer4, Customer4>chunk(chunkSize)
                .reader(asyncPagingItemReader())
                .processor(syncCustomItemProcessor())
                .writer(asyncPagingItemWriter())
                .build();
    }

    @Bean
    public Step asyncStep1() throws Exception {
        return stepBuilderFactory.get("asyncStep1")
                .<Customer4, Customer4>chunk(chunkSize)
                .reader(asyncPagingItemReader())
                .processor(asyncCustomItemProcessor())
                .writer(asyncItemWriter())
                .build();
    }

    @Bean
    public AsyncItemWriter asyncItemWriter() {
        AsyncItemWriter<Customer4> asyncItemWriter = new AsyncItemWriter();
        asyncItemWriter.setDelegate(asyncPagingItemWriter());
        return asyncItemWriter;
    }


    @Bean
    public AsyncItemProcessor asyncCustomItemProcessor() throws InterruptedException {

        AsyncItemProcessor<Customer4, Customer4> asyncItemProcessor = new AsyncItemProcessor<>();
        // ItemProcessor 처리를 위임할 메서드 설정
        asyncItemProcessor.setDelegate(syncCustomItemProcessor());
        // 스레드를 생성할 TaskExecutor 설정
        asyncItemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor());

        return asyncItemProcessor;
    }

    @Bean
    public ItemProcessor<Customer4, Customer4> syncCustomItemProcessor() throws InterruptedException {
        return new ItemProcessor<Customer4, Customer4>() {
            @Override
            public Customer4 process(Customer4 item) throws Exception {

                Thread.sleep(30);

                return new Customer4(item.getId(), item.getFirstName().toUpperCase(),
                        item.getFirstName().toUpperCase(), item.getBirthdate());
            }
        };
    }

    @Bean
    public ItemWriter<Customer4> asyncPagingItemWriter() {
        return items -> {
            for (Customer4 item : items) {
                System.out.println("item = " + item.toString());
            }
        };
    }

    @Bean
    public ItemReader<Customer4> asyncPagingItemReader() throws Exception {

        return new JdbcPagingItemReaderBuilder<Customer4>()
                .name("asyncPagingItemReader")
                .pageSize(chunkSize)
                .beanRowMapper(Customer4.class)
                .dataSource(dataSource)
                .queryProvider(asyncCreateQueryProvider())
                .build();
    }

    @Bean
    public PagingQueryProvider asyncCreateQueryProvider() throws Exception {

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
