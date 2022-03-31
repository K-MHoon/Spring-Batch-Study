package com.example.study.dto;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "email_customer")
@Data
public class EmailCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String middleInitial;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String email;
}
