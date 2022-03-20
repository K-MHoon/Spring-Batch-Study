package com.example.study.service;

import com.example.study.dto.MyCustomer5;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class MyCustomerService {

    private List<MyCustomer5> customers;
    private int curIndex;

    private String[] firstNames = {"Michael", "Warren", "Ann", "Terrence", "Erica", "Laura", "Steve", "Larry"};
    private String middleInitial = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private String[] lastNames = {"Gates", "Darrow", "Donnelly", "Jobs", "Buffett", "Ellison", "Obama"};
    private String[] streets = {"4th Street", "Wall Street", "Fifth Avenue", "Mt. Lee Drive", "Jeopardy Lane", "Infinite Loop Drive", "Farnam Street", "Isabella Ave", "S. Greenwood Ave"};
    private String[] cities = {"Chicago", "New York", "Hollywood", "Aurora", "Omaha", "Atherton"};
    private String[] states = {"IL", "NY", "CA", "NE"};

    private Random generator = new Random();

    public MyCustomerService() {
        curIndex = 0;

        customers = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            customers.add(buildCustomer());
        }
    }

    private MyCustomer5 buildCustomer() {
        MyCustomer5 customer = new MyCustomer5();

        customer.setId((long) generator.nextInt(Integer.MAX_VALUE));
        customer.setFirstName(firstNames[generator.nextInt(firstNames.length-1)]);
        customer.setMiddleInitial(String.valueOf(middleInitial.charAt(generator.nextInt(middleInitial.length()-1))));
        customer.setLastName(lastNames[generator.nextInt(lastNames.length-1)]);
        customer.setAddress(generator.nextInt(9999) + " " + streets[generator.nextInt(streets.length-1)]);
        customer.setCity(cities[generator.nextInt(cities.length-1)]);
        customer.setState(states[generator.nextInt(states.length-1)]);
        customer.setZipCode(String.valueOf(generator.nextInt(99999)));

        return customer;
    }

    public MyCustomer5 getCustomer() {
        MyCustomer5 cust = null;

        if(curIndex < customers.size()) {
            cust = customers.get(curIndex);
            curIndex++;
        }

        return cust;
    }
}
