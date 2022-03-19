package com.example.study.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.data.MongoItemReader;
import org.springframework.batch.item.data.builder.MongoItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;

import java.util.Collections;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class MongoConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final MongoOperations mongoTemplate;

    @Bean
    public Job mongoDBJob() {
        return jobBuilderFactory.get("mongoDBJob")
                .incrementer(new RunIdIncrementer())
                .start(mongoDBStep())
                .build();
    }

    @Bean
    public Step mongoDBStep() {
        return stepBuilderFactory.get("mongoDBStep")
                .<Map, Map>chunk(10)
                .reader(tweetsItemReader(null))
                .writer(items -> {
                    items.forEach(System.out::println);
                })
                .build();
    }

    @Bean
    @StepScope
    public MongoItemReader<Map> tweetsItemReader(
            @Value("#{jobParameters['hashTag']}") String hashTag
    ) {
        return new MongoItemReaderBuilder<Map>()
                .name("tweetsItemReader")
                .targetType(Map.class)
                .jsonQuery("{ \"entities.hashtags.text\": { $eq: ?0 }}")
                .collection("tweets_collection")
                .parameterValues(Collections.singletonList(hashTag))
                .pageSize(10)
                .sorts(Collections.singletonMap("created_at", Sort.Direction.ASC))
                .template(mongoTemplate)
                .build();
    }
}
