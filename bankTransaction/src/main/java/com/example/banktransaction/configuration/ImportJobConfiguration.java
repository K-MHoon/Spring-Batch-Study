package com.example.banktransaction.configuration;

import com.example.banktransaction.classifier.CustomerUpdateClassifier;
import com.example.banktransaction.validator.CustomerItemValidator;
import com.example.banktransaction.vo.CustomerAddressUpdate;
import com.example.banktransaction.vo.CustomerContactUpdate;
import com.example.banktransaction.vo.CustomerNameUpdate;
import com.example.banktransaction.vo.CustomerUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.PatternMatchingCompositeLineTokenizer;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ImportJobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job importJob() throws Exception {
        return jobBuilderFactory.get("importJob")
                .start(importCustomerUpdates())
                .build();
    }

    @Bean
    public Step importCustomerUpdates() throws Exception {
        return stepBuilderFactory.get("importCustomerUpdates")
                .<CustomerUpdate, CustomerUpdate>chunk(100)
                .reader(customerUpdateItemReader(null))
                .processor(customerValidatingItemProcessor(null))
                .writer(customerUpdateItemWriter())
                .build();
    }

    @Bean
    public ItemWriter customerUpdateItemWriter() {
        CustomerUpdateClassifier classifier = new CustomerUpdateClassifier(customerNameUpdateItemWriter(null),
                customerAddressUpdateItemWriter(null),
                customerContactUpdateItemWriter(null));

        ClassifierCompositeItemWriter<CustomerUpdate> compositeItemWriter = new ClassifierCompositeItemWriter<>();
        compositeItemWriter.setClassifier(classifier);
        return compositeItemWriter;
    }

    @Bean
    public ValidatingItemProcessor customerValidatingItemProcessor(CustomerItemValidator validator) {
        ValidatingItemProcessor<CustomerUpdate> customerUpdateValidatingItemProcessor
                = new ValidatingItemProcessor<>(validator);
        customerUpdateValidatingItemProcessor.setFilter(true);
        return customerUpdateValidatingItemProcessor;
    }

    @Bean
    public JdbcBatchItemWriter<CustomerUpdate> customerNameUpdateItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<CustomerUpdate>()
                .beanMapped()
                .sql("update bank_customer set " +
                        "first_name = COALESCE(:firstName, first_name), " +
                        "middle_name = COALESCE(:middleName, middle_name), " +
                        "last_name = COALESCE(:lastName, last_name) " +
                        "where customer_id = :customerId")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<CustomerUpdate> customerAddressUpdateItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<CustomerUpdate>()
                .beanMapped()
                .sql("update bank_customer set " +
                        "address1 = COALESCE(:address1, address1), " +
                        "address2 = COALESCE(:address2, address2), " +
                        "city = COALESCE(:city, city), " +
                        "state = COALESCE(:state, state), " +
                        "postal_code = COALESCE(:postalCode, postal_code) " +
                        "where customer_id = :customerId")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<CustomerUpdate> customerContactUpdateItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<CustomerUpdate>()
                .beanMapped()
                .sql("update bank_customer set " +
                        "email_address = COALESCE(:emailAddress, email_address), " +
                        "home_phone = COALESCE(:homePhone, home_phone), " +
                        "cell_phone = COALESCE(:cellPhone, cell_phone), " +
                        "work_phone = COALESCE(:workPhone, work_phone), " +
                        "notification_pref = COALESCE(:notificationPreferences, notification_pref) " +
                        "where customer_id = :customerId")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<CustomerUpdate> customerUpdateItemReader(
            @Value("#{jobParameters['customerUpdateFile']}") FileSystemResource inputFile) throws Exception {
        return new FlatFileItemReaderBuilder<CustomerUpdate>()
                .name("customerUpdateItemReader")
                .resource(inputFile)
                .lineTokenizer(customerUpdatesLineTokenizer())
                .fieldSetMapper(customerUpdateFieldSetMapper())
                .build();
    }

    @Bean
    public LineTokenizer customerUpdatesLineTokenizer() throws Exception {
        DelimitedLineTokenizer recordType1 = new DelimitedLineTokenizer();
        recordType1.setNames("recordId", "customerId", "firstName", "middleName", "lastName");
        recordType1.afterPropertiesSet();
        DelimitedLineTokenizer recordType2 = new DelimitedLineTokenizer();
        recordType2.setNames("recordId", "customerId", "address1", "address2", "city", "state", "postalCode");
        recordType2.afterPropertiesSet();
        DelimitedLineTokenizer recordType3 = new DelimitedLineTokenizer();
        recordType3.setNames("recordId", "customerId", "emailAddress", "homePhone",
                "cellPhone", "workPhone", "notificationPreference");
        recordType3.afterPropertiesSet();

        Map<String, LineTokenizer> tokenizers = new HashMap<>();
        tokenizers.put("1*", recordType1);
        tokenizers.put("2*", recordType2);
        tokenizers.put("3*", recordType3);
        PatternMatchingCompositeLineTokenizer lineTokenizer = new PatternMatchingCompositeLineTokenizer();
        lineTokenizer.setTokenizers(tokenizers);
        return lineTokenizer;
    }

    @Bean
    public FieldSetMapper<CustomerUpdate> customerUpdateFieldSetMapper() {
        return fieldSet -> {
            switch (fieldSet.readInt("recordId")) {
                case 1 : return new CustomerNameUpdate(
                        fieldSet.readLong("customerId"),
                        fieldSet.readString("firstName"),
                        fieldSet.readString("middleName"),
                        fieldSet.readString("lastName"));
                case 2 : return new CustomerAddressUpdate(
                        fieldSet.readLong("customerId"),
                        fieldSet.readString("address1"),
                        fieldSet.readString("address2"),
                        fieldSet.readString("city"),
                        fieldSet.readString("state"),
                        fieldSet.readString("postalCode"));
                case 3 : String rawPreference = fieldSet.readString("notificationPreference");
                Integer notificationPreference = null;

                if(StringUtils.hasText(rawPreference)) {
                    notificationPreference = Integer.parseInt(rawPreference);
                }
                return new CustomerContactUpdate(
                        fieldSet.readLong("customerId"),
                        fieldSet.readString("emailAddress"),
                        fieldSet.readString("homePhone"),
                        fieldSet.readString("cellPhone"),
                        fieldSet.readString("workPhone"),
                        notificationPreference);
                default: throw new IllegalArgumentException(
                        "Invalid record type was found:" + fieldSet.readInt("recordId"));
            }
        };
    }
}
