package com.example.study.service;

import com.example.study.dto.WriteCustomer;
import org.springframework.stereotype.Service;

@Service
public class WriteCustomerService {

    public void logCustomer(WriteCustomer cust) {
        System.out.println("I just saved " + cust);
    }
}
