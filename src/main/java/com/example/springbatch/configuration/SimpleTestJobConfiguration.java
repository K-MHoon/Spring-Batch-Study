package com.example.springbatch.configuration;

import com.example.springbatch.dto.Customer4;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SimpleTestJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final int chunkSize = 5;

    @Bean
    public Job simpleTestJob() throws Exception {
        return jobBuilderFactory.get("simpleTestJob")
                .incrementer(new RunIdIncrementer())
                .start(simpleTestStep())
                .build();
    }

    @Bean
    public Step simpleTestStep() throws Exception {
        return stepBuilderFactory.get("simpleTestJobStep")
                .<Customer4, Customer4>chunk(chunkSize)
                .reader(simpleTestItemReader())
                .listener(new ItemReadListener<Customer4>() {
                    @Override
                    public void beforeRead() {

                    }

                    @Override
                    public void afterRead(Customer4 item) {
                        System.out.println("read Item = " + item);
                    }

                    @Override
                    public void onReadError(Exception ex) {

                    }
                })
                .writer(simpleTestItemWriter())
                .listener(new ItemWriteListener<Customer4>() {
                    @Override
                    public void beforeWrite(List<? extends Customer4> items) {

                    }

                    @Override
                    public void afterWrite(List<? extends Customer4> items) {
                        for (Customer4 item : items) {
                            System.out.println("written Item = " + item);
                        }
                    }

                    @Override
                    public void onWriteError(Exception exception, List<? extends Customer4> items) {

                    }
                })
                .build();
    }

    @Bean
    public ItemWriter<? super Customer4> simpleTestItemWriter() {
        return new JdbcBatchItemWriterBuilder<Customer4>()
                .dataSource(dataSource)
                .sql("insert into customer2 values (:id, :firstName, :lastName, :birthdate)")
                .beanMapped()
                .build();
    }

    @Bean
    public ItemReader<Customer4> simpleTestItemReader() throws Exception {
        return new JdbcPagingItemReaderBuilder<Customer4>()
                .name("simpleTestItemReader")
                .pageSize(chunkSize)
                .beanRowMapper(Customer4.class)
                .dataSource(dataSource)
                .queryProvider(simpleTestCreateQueryProvider())
                .build();
    }

    public PagingQueryProvider simpleTestCreateQueryProvider() throws Exception {

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
