package com.example.springbatch.listener;

import com.example.springbatch.dto.Customer4;
import org.springframework.batch.core.ItemReadListener;

public class MultiThreadCustomItemReadListener implements ItemReadListener<Customer4> {

    @Override
    public void beforeRead() {

    }

    @Override
    public void afterRead(Customer4 item) {
        System.out.println("Thread : " + Thread.currentThread().getName() + " read item : " + item.getId());
    }

    @Override
    public void onReadError(Exception ex) {

    }
}
