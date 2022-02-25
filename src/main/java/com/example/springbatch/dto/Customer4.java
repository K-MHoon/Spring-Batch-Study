package com.example.springbatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@Table(name = "customer")
@AllArgsConstructor
@NoArgsConstructor
public class Customer4 {

    @Id @GeneratedValue
    private Long id;

    private String firstName;
    private String lastName;
    private String birthdate;
}
