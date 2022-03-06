package com.example.study.listener;

import com.example.study.dto.Customer4;
import org.springframework.batch.core.ItemProcessListener;

public class MultiThreadCustomItemProcessListener implements ItemProcessListener<Customer4, Customer4> {

    @Override
    public void beforeProcess(Customer4 item) {

    }

    @Override
    public void afterProcess(Customer4 item, Customer4 result) {
        System.out.println("Thread : " + Thread.currentThread().getName() + " process item : " + item.getId());
    }

    @Override
    public void onProcessError(Customer4 item, Exception e) {

    }
}
