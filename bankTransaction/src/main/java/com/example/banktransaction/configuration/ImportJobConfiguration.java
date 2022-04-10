package com.example.banktransaction.configuration;

import com.example.banktransaction.aggregator.StatementLineAggregator;
import com.example.banktransaction.classifier.CustomerUpdateClassifier;
import com.example.banktransaction.header.StatementHeaderCallback;
import com.example.banktransaction.processor.AccountItemProcessor;
import com.example.banktransaction.validator.CustomerItemValidator;
import com.example.banktransaction.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemWriter;
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.builder.MultiResourceItemWriterBuilder;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.PatternMatchingCompositeLineTokenizer;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
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
                .incrementer(new RunIdIncrementer())
                .start(importCustomerUpdates())
                .next(importTransactions())
                .next(applyTransactions())
                .next(generateStatements(null))
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
    public Step importTransactions() {
        return this.stepBuilderFactory.get("importTransactions")
                .<Transaction, Transaction>chunk(100)
                .reader(transactionItemReader(null))
                .writer(transactionItemWriter(null))
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    @StepScope
    public StaxEventItemReader<Transaction> transactionItemReader(
            @Value("#{jobParameters['transactionFile']}") FileSystemResource transactionFile) {
        Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
        unmarshaller.setClassesToBeBound(Transaction.class);

        return new StaxEventItemReaderBuilder<Transaction>()
                .name("fooReader")
                .saveState(false)
                .resource(transactionFile)
                .addFragmentRootElements("transaction")
                .unmarshaller(unmarshaller)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Transaction> transactionItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .dataSource(dataSource)
                .sql("INSERT INTO TRANSACTION (TRANSACTION_ID, " +
                        "ACCOUNT_ACCOUNT_ID, " +
                        "DESCRIPTION, " +
                        "CREDIT, " +
                        "DEBIT, " +
                        "TIMESTAMP) VALUES (:transactionId, " +
                        ":accountId, " +
                        ":description, " +
                        ":credit, " +
                        ":debit, " +
                        ":timestamp)")
                .beanMapped()
                .build();
    }

    @Bean
    public Step applyTransactions() {
        return stepBuilderFactory.get("applyTransactions")
                .<Transaction, Transaction>chunk(100)
                .reader(applyTransactionReader(null))
                .writer(applyTransactionWriter(null))
                .build();
    }

    @Bean
    public JdbcBatchItemWriter applyTransactionWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaction>()
                .dataSource(dataSource)
                .sql("UPDATE ACCOUNT SET " +
                        "BALANCE = BALANCE + :transactionAmount " +
                        "WHERE ACCOUNT_ID = :accountId")
                .beanMapped()
                .assertUpdates(false)
                .build();
    }

    @Bean
    public Step generateStatements(AccountItemProcessor itemProcessor) {
        return stepBuilderFactory.get("generateStatements")
                .<Statement, Statement>chunk(1)
                .reader(statementItemReader(null))
                .processor(itemProcessor)
                .writer(statementItemWriter(null))
                .build();
    }

    @Bean
    @StepScope
    public MultiResourceItemWriter statementItemWriter(
            @Value("#{jobParameters['outputDirectory']}") FileSystemResource outputDir) {
        return new MultiResourceItemWriterBuilder<Statement>()
                .name("statementItemWriter")
                .resource(outputDir)
                .itemCountLimitPerResource(1)
                .delegate(individualStatementItemWriter())
                .build();
    }

    @Bean
    public FlatFileItemWriter individualStatementItemWriter() {
        return new FlatFileItemWriterBuilder<Statement>()
                .name("individualStatementItemWriter")
                .headerCallback(new StatementHeaderCallback())
                .lineAggregator(new StatementLineAggregator())
                .build();
    }

    @Bean
    public JdbcCursorItemReader statementItemReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Statement>()
                .name("statementItemReader")
                .dataSource(dataSource)
                .sql("SELECT * FROM BANK_CUSTOMER")
                .rowMapper((resultSet, i) -> {
                    Customer customer = Customer.builder()
                            .id(resultSet.getLong("customer_id"))
                            .firstName(resultSet.getString("first_name"))
                            .middleName(resultSet.getString("middle_name"))
                            .lastName(resultSet.getString("last_name"))
                            .address1(resultSet.getString("address1"))
                            .address2(resultSet.getString("address2"))
                            .city(resultSet.getString("city"))
                            .state(resultSet.getString("state"))
                            .postalCode(resultSet.getString("postal_code"))
                            .ssn(resultSet.getString("ssn"))
                            .emailAddress(resultSet.getString("email_address"))
                            .homePhone(resultSet.getString("home_phone"))
                            .cellPhone(resultSet.getString("cell_phone"))
                            .workPhone(resultSet.getString("work_phone"))
                            .notificationPreferences(resultSet.getInt("notification_pref"))
                            .build();
                    return new Statement(customer);
                })
                .build();
    }


    @Bean
    public JdbcCursorItemReader applyTransactionReader(DataSource dataSource) {
        return new JdbcCursorItemReaderBuilder<Transaction>()
                .name("applyTransactionReader")
                .dataSource(dataSource)
                .sql("select transaction_id, " +
                        "account_account_id, " +
                        "description, " +
                        "credit, " +
                        "debit, " +
                        "timestamp " +
                        "from transaction " +
                        "order by timestamp")
                .rowMapper((resultSet, i) -> new Transaction(
                        resultSet.getLong("transaction_id"),
                        resultSet.getLong("account_account_id"),
                        resultSet.getString("description"),
                        resultSet.getBigDecimal("credit"),
                        resultSet.getBigDecimal("debit"),
                        resultSet.getTimestamp("timestamp")))
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
