package com.example.banktransaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {JobTests.BatchConfiguration.class,
        BatchAutoConfiguration.class})
@SpringBatchTest
public class JobTests {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    public void test() throws Exception {
        JobExecution jobExecution = this.jobLauncherTestUtils.launchJob();

        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

        StepExecution stepExecution = jobExecution.getStepExecutions().iterator().next();
        assertEquals(BatchStatus.COMPLETED, stepExecution.getStatus());
        assertEquals(3, stepExecution.getReadCount());
        assertEquals(3, stepExecution.getWriteCount());

    }

    @Configuration
    @EnableBatchProcessing
    public static class BatchConfiguration {

        @Autowired
        private JobBuilderFactory jobBuilderFactory;

        @Autowired
        private StepBuilderFactory stepBuilderFactory;

        @Bean
        public ListItemReader<String> itemReader() {
            return new ListItemReader<>(Arrays.asList("foo", "bar", "baz"));
        }

        @Bean
        public ItemWriter<String> itemWriter() {
            return (list -> {
                list.forEach(System.out::println);
            });
        }

        @Bean
        public Step step1() {
            return this.stepBuilderFactory.get("step1")
                    .<String, String> chunk(10)
                    .reader(itemReader())
                    .writer(itemWriter())
                    .build();
        }

        @Bean
        public Job job() {
            return this.jobBuilderFactory.get("job")
                    .start(step1())
                    .build();
        }

        @Bean
        public DataSource dataSource() {
            return new EmbeddedDatabaseBuilder().build();
        }
    }
}
