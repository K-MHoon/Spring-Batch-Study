package com.example.study.reader;

import com.example.study.dto.MyCustomer3;
import com.example.study.dto.Transaction2;
import org.springframework.batch.item.*;

import java.util.ArrayList;

public class CustomerFileReader implements ItemStreamReader<MyCustomer3> {

    private Object curItem = null;
    private ItemStreamReader<Object> delegate;

    public CustomerFileReader(ItemStreamReader<Object> delegate) {
        this.delegate = delegate;
    }

    private Object peek() throws Exception {
        if (curItem == null) {
            curItem = delegate.read();
        }
        return curItem;
    }


    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        delegate.open(executionContext);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        delegate.update(executionContext);
    }

    @Override
    public void close() throws ItemStreamException {
        delegate.close();
    }

    @Override
    public MyCustomer3 read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (curItem == null) {
            curItem = delegate.read();
        }

        MyCustomer3 item = (MyCustomer3) curItem;
        curItem = null;
        if (item != null) {
            item.setTransactions(new ArrayList<>());

            while (peek() instanceof Transaction2) {
                item.getTransactions().add((Transaction2) curItem);
                curItem = null;
            }
        }

        return item;
    }
}
