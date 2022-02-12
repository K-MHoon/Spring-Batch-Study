package com.example.springbatch.processor;

import com.example.springbatch.dto.Customer;
import org.springframework.batch.item.ItemProcessor;

public class CustomItemProcessor implements ItemProcessor<Customer, Customer> {

    @Override
    public Customer process(Customer customer) throws Exception {

        customer.setName(customer.getName().toUpperCase());

        return customer;
    }
}
