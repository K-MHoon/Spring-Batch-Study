package com.example.springbatch.processor;

import com.example.springbatch.dto.Customer;
import com.example.springbatch.exception.RetryableException;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.classify.BinaryExceptionClassifier;
import org.springframework.classify.Classifier;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.support.DefaultRetryState;
import org.springframework.retry.support.RetryTemplate;

public class RetryItemProcessor2 implements ItemProcessor<String, Customer> {

    @Autowired
    private RetryTemplate retryTemplate;

    Classifier<Throwable, Boolean> rollbackClassifier = new BinaryExceptionClassifier(true);

    private int cnt;

    @Override
    public Customer process(String item) throws Exception {

        Customer customer = retryTemplate.execute(
                new RetryCallback<Customer, RuntimeException>() {
                    @Override
                    public Customer doWithRetry(RetryContext context) throws RuntimeException {

                        if(item.equals("1") || item.equals("2")) {
                            cnt++;
                            throw new RetryableException("failed cnt : " + cnt);
                        }
                        return new Customer(item);
                    }
                },
                new RecoveryCallback<Customer>() {
                    @Override
                    public Customer recover(RetryContext context) throws Exception {
                        return new Customer(item);
                    }
                }
                , new DefaultRetryState(item, rollbackClassifier)
        );
        return customer;
    }
}
