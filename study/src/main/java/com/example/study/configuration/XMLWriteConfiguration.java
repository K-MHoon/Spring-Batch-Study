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
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class XMLWriteConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private int chunkSize = 10;

    @Bean
    public Job xmlWriteJob() throws Exception {
        return jobBuilderFactory.get("xmlWriteJob")
                .incrementer(new RunIdIncrementer())
                .start(xmlWriteStep1())
                .build();
    }

    @Bean
    public Step xmlWriteStep1() throws Exception {
        return stepBuilderFactory.get("xmlWriteStep1")
                .<Customer4, Customer4> chunk(chunkSize)
                .reader(xmlWriteReader())
                .writer(xmlWriteWriter())
                .build();
    }

    @Bean
    public ItemWriter<? super Customer4> xmlWriteWriter() {
        return new StaxEventItemWriterBuilder<Customer4>()
                .name("xmlWriteWriter")
                .marshaller(itemMarshaller())
                .shouldDeleteIfEmpty(true)
                .resource(new FileSystemResource("C:\\Users\\MHK\\Desktop\\SpringBatch\\src\\main\\resources\\xmlCustomer.xml"))
                .rootTagName("customer")
                .build();
    }

    @Bean
    public Marshaller itemMarshaller() {

        Map<String, Class<?>> aliases = new HashMap<>();
        aliases.put("customer", Customer4.class);
        aliases.put("id", Long.class);
        aliases.put("firstName", String.class);
        aliases.put("lastName", String.class);
        aliases.put("birthdate", String.class);

        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(aliases);

        return marshaller;
    }

    @Bean
    public ItemReader<? extends Customer4> xmlWriteReader() throws Exception {

        JdbcPagingItemReader<Customer4> reader = new JdbcPagingItemReader<>();
        reader.setDataSource(dataSource);
        reader.setFetchSize(chunkSize);
        reader.setRowMapper(new BeanPropertyRowMapper<>(Customer4.class));

        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause("id, first_name, last_name, birthdate");
        factoryBean.setFromClause("from customer");
        factoryBean.setWhereClause("where first_name like :first_name");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.ASCENDING);
        factoryBean.setSortKeys(sortKeys);
        reader.setQueryProvider(factoryBean.getObject());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("first_name", "A%");

        reader.setParameterValues(parameters);
        return reader;
    }




}
