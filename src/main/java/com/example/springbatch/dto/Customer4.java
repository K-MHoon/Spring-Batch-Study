package com.example.springbatch.dto;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "customer")
public class Customer4 {

    @Id @GeneratedValue
    private Long id;

    private String firstName;
    private String lastName;
    private String birthdate;
}
