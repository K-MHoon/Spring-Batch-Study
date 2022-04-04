package com.example.study.classifier;

import com.example.study.dto.EmailCustomer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

public class CustomerClassifier implements Classifier<EmailCustomer, ItemWriter<? super EmailCustomer>> {

    private ItemWriter<EmailCustomer> fileItemWriter;
    private ItemWriter<EmailCustomer> jdbcItemWriter;

    public CustomerClassifier(ItemWriter<EmailCustomer> fileItemWriter, ItemWriter<EmailCustomer> jdbcItemWriter) {
        this.fileItemWriter = fileItemWriter;
        this.jdbcItemWriter = jdbcItemWriter;
    }

    @Override
    public ItemWriter<? super EmailCustomer> classify(EmailCustomer classifiable) {
        if(classifiable.getState().matches("^[A-M].*")) {
            return fileItemWriter;
        } else {
            return jdbcItemWriter;
        }
    }
}
