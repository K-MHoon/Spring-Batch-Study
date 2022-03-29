package com.example.study.service;

import com.example.study.dto.WriteCustomer;
import org.springframework.stereotype.Service;

@Service
public class WriteCustomerService {

    public void logCustomer(WriteCustomer cust) {
        System.out.println("I just saved " + cust);
    }

    public void logCustomerAddress(String address,
                                   String city,
                                   String state,
                                   String zipCode) {
        System.out.println(
                String.format("I just saved the address:\n%s \n%s, %s\n%s",
                        address,
                        city,
                        state,
                        zipCode));
    }
}
