package com.example.study.configuration;

import com.example.study.callback.CustomerRecordCountFooterCallback;
import com.example.study.dto.EmailCustomer;
import com.example.study.dto.MyCustomer5;
import com.example.study.dto.MyCustomer5;
import com.example.study.suffix.CustomerOutputFileSuffixCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemWriter;
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class MultiResourceItemConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;


    @Bean
    public JdbcCursorItemReader<MyCustomer5> multiResourceItemJdbcCursorItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<MyCustomer5>()
                .name("multiResourceItemJdbcCursorItemReader")
                .dataSource(dataSource)
                .sql("select * from tbl_customer")
                .beanRowMapper(MyCustomer5.class)
                .build();
    }

    @Bean
    public StaxEventItemWriter<MyCustomer5> multiResourceItemWriterToMyCustomer5() {

        Map<String, Class> aliases = new HashMap<>();
        aliases.put("customer", MyCustomer5.class);

        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(aliases);
        marshaller.afterPropertiesSet();

        return new StaxEventItemWriterBuilder<MyCustomer5>()
                .name("multiResourceItemWriterToMyCustomer5")
                .marshaller(marshaller)
                .rootTagName("customers")
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<MyCustomer5> delegateMyCustomer5ItemWriter(
            CustomerRecordCountFooterCallback footerCallback
    ) {
        BeanWrapperFieldExtractor<MyCustomer5> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"firstName", "lastName", "address", "city", "state", "zipCode"});
        fieldExtractor.afterPropertiesSet();

        FormatterLineAggregator<MyCustomer5> lineAggregator = new FormatterLineAggregator<>();
        lineAggregator.setFormat("%s %s lives at %s %s in %s, %s.");
        lineAggregator.setFieldExtractor(fieldExtractor);

        FlatFileItemWriter<MyCustomer5> itemWriter = new FlatFileItemWriter<>();
        itemWriter.setName("delegateMyCustomer5ItemWriter");
        itemWriter.setLineAggregator(lineAggregator);
        itemWriter.setAppendAllowed(true);
        itemWriter.setFooterCallback(footerCallback);
        return itemWriter;
    }

    @Bean
    public MultiResourceItemWriter<MyCustomer5> multiCustomerFileWriter(
            CustomerOutputFileSuffixCreator suffixCreator
    ) {

        return new MultiResourceItemWriterBuilder<MyCustomer5>()
                .name("multiCustomerFileWriter")
                .delegate(delegateMyCustomer5ItemWriter(null))
                .itemCountLimitPerResource(100)
                .resource(new FileSystemResource("study/src/main/resources/output/multiFile/customer"))
                .resourceSuffixCreator(suffixCreator)
                .build();
    }

    @Bean
    public Step multiXmlGeneratorStep() {
        return stepBuilderFactory.get("multiXmlGeneratorStep")
                .<MyCustomer5, MyCustomer5>chunk(100)
                .reader(multiResourceItemJdbcCursorItemReader(null))
                .writer(multiCustomerFileWriter(null))
                .build();
    }

    @Bean
    public Job xmlGeneratorJob() {
        return jobBuilderFactory.get("xmlGeneratorJob")
                .incrementer(new RunIdIncrementer())
                .start(multiXmlGeneratorStep())
                .build();
    }
}
