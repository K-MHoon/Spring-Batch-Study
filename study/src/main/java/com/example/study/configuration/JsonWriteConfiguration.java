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
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class JsonWriteConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private int chunkSize = 10;

    @Bean
    public Job jsonWriteJob() throws Exception {
        return jobBuilderFactory.get("jsonWriteJob")
                .incrementer(new RunIdIncrementer())
                .start(jsonWriteStep1())
                .build();
    }

    @Bean
    public Step jsonWriteStep1() throws Exception {
        return stepBuilderFactory.get("jsonWriteStep1")
                .<Customer4, Customer4> chunk(chunkSize)
                .reader(jsonWriteReader())
                .writer(jsonWriteWriter())
                .build();
    }

    @Bean
    public ItemWriter<? super Customer4> jsonWriteWriter() {
        return new JsonFileItemWriterBuilder<Customer4>()
                .name("jsonWriteWriter")
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .shouldDeleteIfEmpty(true)
                .resource(new FileSystemResource("C:\\Users\\MHK\\Desktop\\SpringBatch\\src\\main\\resources\\jsonCustomer.json"))
                .build();
    }

    @Bean
    public ItemReader<? extends Customer4> jsonWriteReader() throws Exception {

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
                .name("jsonWriteReader")
                .dataSource(dataSource)
                .fetchSize(chunkSize)
                .beanRowMapper(Customer4.class)
                .parameterValues(parameters)
                .queryProvider(factoryBean.getObject())
                .build();
    }
}
