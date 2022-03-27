package com.example.study.dto;

import lombok.Data;

@Data
public class WriteCustomer {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String firstName;
    private String middleInitial;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String zipCode;
}
