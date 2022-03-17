package com.example.study.configuration;

import com.example.study.dto.MyCustomer4;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ArgumentPreparedStatementSetter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

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
                .sql("select * from tbl_customer where city = ?")
                .preparedStatementSetter(citySetter(null))
                .build();
    }

    @Bean
    @StepScope
    public ArgumentPreparedStatementSetter citySetter(
            @Value("#{jobParameters['city']}") String city
    ) {
        return new ArgumentPreparedStatementSetter(new Object[]{city});
    }

    @Bean
    public Job jdbcItemCursorJob() throws Exception {
        return jobBuilderFactory.get("jdbcItemCursorJob")
                .incrementer(new RunIdIncrementer())
                .start(jdbcItemCursorStep())
                .build();
    }

    @Bean
    public Step jdbcItemCursorStep() throws Exception {
        return stepBuilderFactory.get("jdbcItemCursorStep")
                .<MyCustomer4, MyCustomer4>chunk(10)
//                .reader(jdbcItemCursorReader())
                .reader(jdbcItemPagingReader(null))
                .writer(jdbcItemCursorWriter())
                .build();
    }

    @Bean
    public ItemWriter jdbcItemCursorWriter() {
        return (items) -> items.forEach(System.out::println);
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<MyCustomer4> jdbcItemPagingReader(
            @Value("#{jobParameters['city']}") String city
    ) throws Exception {
        Map<String, Object> parameterValues = new HashMap<>();
        parameterValues.put("city", city);
        return new JdbcPagingItemReaderBuilder<MyCustomer4>()
                .name("jdbcItemPagingReader")
                .dataSource(dataSource)
                .beanRowMapper(MyCustomer4.class)
                .queryProvider(pagingQueryProvider())
                .pageSize(10)
                .parameterValues(parameterValues)
                .build();
    }

    @Bean
    public PagingQueryProvider pagingQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean queryProviderFactoryBean = new SqlPagingQueryProviderFactoryBean();

        queryProviderFactoryBean.setDataSource(dataSource);
        queryProviderFactoryBean.setSelectClause("select *");
        queryProviderFactoryBean.setFromClause("from tbl_customer");
        queryProviderFactoryBean.setWhereClause("where city = :city");
        queryProviderFactoryBean.setSortKey("lastName");
        return queryProviderFactoryBean.getObject();
    }
}
