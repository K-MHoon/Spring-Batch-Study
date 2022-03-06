package com.example.study.configuration;

import com.example.study.dto.Customer4;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class JdbcBatchWriteConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private int chunkSize = 10;

    @Bean
    public Job jdbcBatchWriteJob() throws Exception {
        return jobBuilderFactory.get("jdbcBatchWriteJob")
                .incrementer(new RunIdIncrementer())
                .start(jdbcBatchWriteStep1())
                .build();
    }

    @Bean
    public Step jdbcBatchWriteStep1() throws Exception {
        return stepBuilderFactory.get("jdbcBatchWriteStep1")
                .<Customer4, Customer4> chunk(chunkSize)
                .reader(jdbcBatchWriteReader())
                .writer(jdbcBatchWriteWriter())
                .build();
    }

    @Bean
    public ItemWriter<? super Customer4> jdbcBatchWriteWriter() {
        return new JdbcBatchItemWriterBuilder<Customer4>()
                .dataSource(dataSource)
                .sql("insert into customer2 values (:id, :firstName, :lastName, :birthdate)")
                .beanMapped()
                .build();
    }

    @Bean
    public ItemReader<? extends Customer4> jdbcBatchWriteReader() throws Exception {

        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause("id, first_name, last_name, birthdate");
        factoryBean.setFromClause("from customer");
        factoryBean.setWhereClause("where first_name like :first_name");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.ASCENDING);
        factoryBean.setSortKeys(sortKeys);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("first_name", "A%");

        return  new JdbcPagingItemReaderBuilder<Customer4>()
                .name("jdbcBatchWriteReader")
                .dataSource(dataSource)
                .fetchSize(chunkSize)
                .beanRowMapper(Customer4.class)
                .parameterValues(parameters)
                .queryProvider(factoryBean.getObject())
                .build();
    }
}
