package com.example.study.service;

import com.example.study.dto.MyCustomer2;
import org.springframework.stereotype.Service;

@Service
public class LowerCaseNameService {

    public MyCustomer2 lowerCase(MyCustomer2 customer) {
       customer.setFirstName(customer.getFirstName().toLowerCase());
       customer.setMiddleInitial(customer.getMiddleInitial().toLowerCase());
       customer.setLastName(customer.getLastName().toLowerCase());
       return customer;
    }
}
