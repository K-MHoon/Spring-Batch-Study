package com.example.study.configuration;

import com.example.study.dto.Customer3;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class XMLConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job xmlJob() {
        return jobBuilderFactory.get("xmlJob")
                .incrementer(new RunIdIncrementer())
                .start(xmlStep1())
                .build();
    }

    @Bean
    public Step xmlStep1() {
        return stepBuilderFactory.get("xmlStep1")
                .<Customer3, Customer3>chunk(3)
                .reader(xmlItemReader())
                .writer(xmlItemWriter())
                .build();
    }

    @Bean
    public ItemWriter<? super Customer3> xmlItemWriter() {
        return items -> {
            for (Customer3 item : items) {
                System.out.println(item.toString());
            }
        };
    }

    @Bean
    public ItemReader<? extends Customer3> xmlItemReader() {
        return new StaxEventItemReaderBuilder<Customer3>()
                .name("staXml")
                .resource(new ClassPathResource("/customer.xml"))
                .addFragmentRootElements("customer")
                .unmarshaller(itemUnmarshaller())
                .build();
    }

    @Bean
    public Unmarshaller itemUnmarshaller() {
        Map<String, Class<?>> aliases = new HashMap<>();
        aliases.put("customer", Customer3.class);
        aliases.put("id", Long.class);
        aliases.put("name", String.class);
        aliases.put("age", Integer.class);

        XStreamMarshaller xStreamMarshaller = new XStreamMarshaller();
        xStreamMarshaller.setAliases(aliases);

        return xStreamMarshaller;
    }

}
