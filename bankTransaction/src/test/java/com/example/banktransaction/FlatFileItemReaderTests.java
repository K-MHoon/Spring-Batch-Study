package com.example.banktransaction;

import com.example.banktransaction.configuration.ImportJobConfiguration;
import com.example.banktransaction.processor.AccountItemProcessor;
import com.example.banktransaction.validator.CustomerItemValidator;
import com.example.banktransaction.vo.CustomerAddressUpdate;
import com.example.banktransaction.vo.CustomerContactUpdate;
import com.example.banktransaction.vo.CustomerNameUpdate;
import com.example.banktransaction.vo.CustomerUpdate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;

import java.net.URISyntaxException;
import java.nio.file.Paths;

@ContextConfiguration(classes = {ImportJobConfiguration.class,
        CustomerItemValidator.class,
        AccountItemProcessor.class})
@JdbcTest
@EnableBatchProcessing
@SpringBatchTest
public class FlatFileItemReaderTests {

    @Autowired
    private FlatFileItemReader<CustomerUpdate> customerUpdateItemReader;

    public StepExecution getStepExecution() throws URISyntaxException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("customerUpdateFile", Paths.get(this.getClass().getResource("/input/customerUpdateFile.csv").toURI()).toString())
                .toJobParameters();

        return MetaDataInstanceFactory.createStepExecution(jobParameters); // stepExecution 생성
    }

    @Test
    public void testTypeConversion() throws Exception {
        this.customerUpdateItemReader.open(new ExecutionContext());

        Assertions.assertTrue(this.customerUpdateItemReader.read() instanceof CustomerAddressUpdate);
        Assertions.assertTrue(this.customerUpdateItemReader.read() instanceof CustomerContactUpdate);
        Assertions.assertTrue(this.customerUpdateItemReader.read() instanceof CustomerNameUpdate);
    }
}
