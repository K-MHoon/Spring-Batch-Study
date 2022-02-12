package com.example.springbatch.reader;

import com.example.springbatch.dto.Customer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;

import java.util.List;

public class CustomItemStreamWriter implements ItemStreamWriter<Customer> {
    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        System.out.println("write open");
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        System.out.println("write update");
    }

    @Override
    public void close() throws ItemStreamException {
        System.out.println("write close");
    }

    @Override
    public void write(List<? extends Customer> list) throws Exception {
        list.forEach(i -> System.out.println(i));
    }
}
