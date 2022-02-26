package com.example.springbatch.configuration;

import com.example.springbatch.dto.Customer4;
import com.example.springbatch.partitioner.ColumnRangePartitioner;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class PartitioningConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final int chunkSize = 5;
    
    @Bean
    public Job partitioningJob() throws Exception {
        return jobBuilderFactory.get("partitioningJob")
                .incrementer(new RunIdIncrementer())
                .start(masterStep())
                .build();
    }

    @Bean
    public Step masterStep() throws Exception {
        return stepBuilderFactory.get("masterStep")
                .partitioner(slaveStep().getName(), partitioner())
                .step(slaveStep())
                .gridSize(4)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Partitioner partitioner() {
        ColumnRangePartitioner partitioner = new ColumnRangePartitioner();

        partitioner.setColumn("id");
        partitioner.setDataSource(dataSource);
        partitioner.setTable("customer");

        return partitioner;
    }

    @Bean
    public Step slaveStep() throws Exception {
        return stepBuilderFactory.get("slaveStep")
                .<Customer4, Customer4>chunk(chunkSize)
                .reader(partitioningPagingItemReader(null, null))
                .writer(partitioningPagingItemWriter())
                .build();
    }

    @Bean
    @StepScope
    public ItemWriter<? super Customer4> partitioningPagingItemWriter() {
        return new JdbcBatchItemWriterBuilder<Customer4>()
                .dataSource(dataSource)
                .sql("insert into customer2 values (:id, :firstName, :lastName, :birthdate)")
                .beanMapped()
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Customer4> partitioningPagingItemReader(
            @Value("#{stepExecutionContext['minValue']}") Long minValue,
            @Value("#{stepExecutionContext['maxValue']}") Long maxValue
    ) throws Exception {

        System.out.println(String.format("reading : %s to %s", minValue, maxValue));

        return new JdbcPagingItemReaderBuilder<Customer4>()
                .name("partitioningPagingItemReader")
                .pageSize(chunkSize)
                .beanRowMapper(Customer4.class)
                .dataSource(dataSource)
                .queryProvider(partitioningCreateQueryProvider(minValue, maxValue))
                .build();
    }

    public PagingQueryProvider partitioningCreateQueryProvider(Long minValue, Long maxValue) throws Exception {

        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setSelectClause("id, first_name, last_name, birthdate");
        factoryBean.setFromClause("from customer");
        factoryBean.setWhereClause("where id >= " + minValue + " and id <= " + maxValue);

        Map<String, Order> sortKey = new HashMap<>();
        sortKey.put("id", Order.ASCENDING);

        factoryBean.setSortKeys(sortKey);

        return factoryBean.getObject();
    }
}
