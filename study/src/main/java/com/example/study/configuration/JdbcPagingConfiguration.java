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
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class JdbcPagingConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private int chunkSize = 10;
    private final DataSource dataSource;

    @Bean
    public Job jdbcPagingJob() throws Exception {
        return jobBuilderFactory.get("jdbcPagingJob")
                .incrementer(new RunIdIncrementer())
                .start(jdbcPagingStep1())
                .build();
    }

    @Bean
    public Step jdbcPagingStep1() throws Exception {
        return stepBuilderFactory.get("jdbcPagingStep1")
                .<Customer4, Customer4>chunk(chunkSize)
                .reader(jdbcPagingItemReader())
                .writer(jdbcPagingItemWriter())
                .build();
    }

    @Bean
    public ItemReader<Customer4> jdbcPagingItemReader() throws Exception {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("firstname", "Iliana");

        return new JdbcPagingItemReaderBuilder<Customer4>()
                .name("jdbcPagingItemReader")
                .pageSize(chunkSize)
                .beanRowMapper(Customer4.class)
                .dataSource(dataSource)
                .queryProvider(createQueryProvider())
                .parameterValues(parameters)
                .build();
    }

    @Bean
    public PagingQueryProvider createQueryProvider() throws Exception {

        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause("id, first_name, last_name, birthdate");
        factoryBean.setFromClause("from customer");
        factoryBean.setWhereClause("where first_name like :firstname");

        Map<String, Order> sortKey = new HashMap<>();
        sortKey.put("id", Order.ASCENDING);

        factoryBean.setSortKeys(sortKey);

        return factoryBean.getObject();
    }

    @Bean
    public ItemWriter<? super Customer4> jdbcPagingItemWriter() {
        return items -> {
            for (Customer4 item : items) {
                System.out.println("item = " + item.toString());
            }
        };
    }

}
