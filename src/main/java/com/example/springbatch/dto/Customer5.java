package com.example.springbatch.dto;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "customer2")
public class Customer5 {

    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String birthdate;
}
