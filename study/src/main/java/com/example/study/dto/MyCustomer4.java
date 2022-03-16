package com.example.study.dto;

import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class MyCustomer4 {

    private Long id;
    private String firstName;
    private String middleInitial;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String zipCode;
}
