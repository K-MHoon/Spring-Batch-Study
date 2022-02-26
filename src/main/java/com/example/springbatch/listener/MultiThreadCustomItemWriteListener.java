package com.example.springbatch.listener;

import com.example.springbatch.dto.Customer4;
import org.springframework.batch.core.ItemWriteListener;

import java.util.List;

public class MultiThreadCustomItemWriteListener implements ItemWriteListener<Customer4> {

    @Override
    public void beforeWrite(List<? extends Customer4> items) {

    }

    @Override
    public void afterWrite(List<? extends Customer4> items) {
        System.out.println("Thread : " + Thread.currentThread().getName() + " write items : " + items.size());
    }

    @Override
    public void onWriteError(Exception exception, List<? extends Customer4> items) {

    }
}
