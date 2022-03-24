package com.example.study.service;

import com.example.study.dto.MyCustomer2;
import org.springframework.stereotype.Service;

@Service
public class UpperCaseNameService {

    public MyCustomer2 upperCase(MyCustomer2 customer) {
       customer.setFirstName(customer.getFirstName().toUpperCase());
       customer.setMiddleInitial(customer.getMiddleInitial().toUpperCase());
       customer.setLastName(customer.getLastName().toUpperCase());
       return customer;
    }
}
