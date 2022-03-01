package com.example.demo.batch.job.api;

import com.example.demo.batch.chunk.processor.ApiItemProcessor1;
import com.example.demo.batch.chunk.processor.ApiItemProcessor2;
import com.example.demo.batch.chunk.processor.ApiItemProcessor3;
import com.example.demo.batch.chunk.writer.ApiItemWriter1;
import com.example.demo.batch.chunk.writer.ApiItemWriter2;
import com.example.demo.batch.chunk.writer.ApiItemWriter3;
import com.example.demo.batch.classifier.ProcessorClassifier;
import com.example.demo.batch.classifier.WriterClassifier;
import com.example.demo.batch.domain.ApiRequestVO;
import com.example.demo.batch.domain.ProductVO;
import com.example.demo.batch.partition.ProductPartitioner;
import com.example.demo.service.ApiService1;
import com.example.demo.service.ApiService2;
import com.example.demo.service.ApiService3;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ApiStepConfiguration {

    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final ApiService1 apiService1;
    private final ApiService2 apiService2;
    private final ApiService3 apiService3;
    private int chunkSize = 10;

    @Bean
    public Step apiMasterStep() throws Exception {

        ProductVO[] productList = QueryGenerator.getProductList(dataSource);

        return stepBuilderFactory.get("apiMasterStep")
                .partitioner(apiSlaveStep().getName(), partitioner())
                .step(apiSlaveStep())
                .gridSize(productList.length)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(3);
        taskExecutor.setMaxPoolSize(6);
        taskExecutor.setThreadNamePrefix("api-thread-");
        return taskExecutor;
    }

    @Bean
    public Step apiSlaveStep() throws Exception {
        return stepBuilderFactory.get("apiSlaveStep")
                .<ProductVO, ProductVO>chunk(chunkSize)
                .reader(itemReader(null))
                .processor(itemProcessor())
                .writer(itemWriter())
                .build();
    }

    @Bean
    public ItemWriter<? super ApiRequestVO> itemWriter() {
        Map<String, ItemWriter<ApiRequestVO>> writerMap = new HashMap<>();
        writerMap.put("1", new ApiItemWriter1(apiService1));
        writerMap.put("2", new ApiItemWriter2(apiService2));
        writerMap.put("3", new ApiItemWriter3(apiService3));

        WriterClassifier<ApiRequestVO, ItemWriter<? super ApiRequestVO>> classifier
                = new WriterClassifier<>();
        classifier.setWriterMap(writerMap);

        ClassifierCompositeItemWriter<ApiRequestVO> writer
                = new ClassifierCompositeItemWriter<>();
        writer.setClassifier(classifier);

        return writer;
    }

    @Bean
    public ItemProcessor itemProcessor() {
        Map<String, ItemProcessor<ProductVO, ApiRequestVO>> processorMap = new HashMap<>();
        processorMap.put("1", new ApiItemProcessor1());
        processorMap.put("2", new ApiItemProcessor2());
        processorMap.put("3", new ApiItemProcessor3());

        ProcessorClassifier<ProductVO, ItemProcessor<?, ? extends ApiRequestVO>> classifier
                = new ProcessorClassifier<>();
        classifier.setProcessorMap(processorMap);

        ClassifierCompositeItemProcessor<ProductVO, ApiRequestVO> processor
                = new ClassifierCompositeItemProcessor<>();
        processor.setClassifier(classifier);

        return processor;
    }

    @Bean
    public ProductPartitioner partitioner() {
        ProductPartitioner productPartitioner = new ProductPartitioner();
        productPartitioner.setDataSource(dataSource);
        return productPartitioner;
    }

    @Bean
    @StepScope
    public ItemReader<ProductVO> itemReader(@Value("#{stepExecutionContext['product']}") ProductVO productVO) throws Exception {
        return new JdbcPagingItemReaderBuilder<ProductVO>()
                .name("itemReader")
                .pageSize(chunkSize)
                .beanRowMapper(ProductVO.class)
                .dataSource(dataSource)
                .queryProvider(createQueryProvider())
                .parameterValues(QueryGenerator.getParameterForQuery("type", productVO.getType()))
                .build();
    }

    @Bean
    public PagingQueryProvider createQueryProvider() throws Exception {

        MySqlPagingQueryProvider factoryBean = new MySqlPagingQueryProvider();
        factoryBean.setSelectClause("id, name, price, type");
        factoryBean.setFromClause("from product");
        factoryBean.setWhereClause("where type = :type");

        Map<String, Order> sortKey = new HashMap<>();
        sortKey.put("id", Order.ASCENDING);
        factoryBean.setSortKeys(sortKey);

        return factoryBean;
    }


}
