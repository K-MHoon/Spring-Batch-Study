package com.example.study.configuration;

import com.example.study.dto.MyCustomer3;
import com.example.study.dto.MyCustomer4;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class JdbcItemConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    @Bean
    public JdbcCursorItemReader<MyCustomer4> jdbcItemCursorReader() {
        return new JdbcCursorItemReaderBuilder<MyCustomer4>()
                .name("jdbcItemCursorReader")
                .beanRowMapper(MyCustomer4.class)
                .dataSource(dataSource)
                .sql("select * from tbl_customer")
                .build();
    }

    @Bean
    public Job jdbcItemCursorJob() {
        return jobBuilderFactory.get("jdbcItemCursorJob")
                .incrementer(new RunIdIncrementer())
                .start(jdbcItemCursorStep())
                .build();
    }

    @Bean
    public Step jdbcItemCursorStep() {
        return stepBuilderFactory.get("jdbcItemCursorStep")
                .<MyCustomer4, MyCustomer4>chunk(10)
                .reader(jdbcItemCursorReader())
                .writer(jdbcItemCursorWriter())
                .build();
    }

    @Bean
    public ItemWriter jdbcItemCursorWriter() {
        return (items) -> items.forEach(System.out::println);
    }
}
