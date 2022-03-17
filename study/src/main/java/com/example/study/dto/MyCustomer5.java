package com.example.study.dto;

import lombok.*;

import javax.persistence.*;

@NoArgsConstructor
@Data
@Entity(name = "Customer")
@Table(name = "tbl_customer")
public class MyCustomer5 {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String middleInitial;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String zipCode;
}
