package com.example.study.dto;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
public class WriteCustomerByMongo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;
    private String firstName;
    private String middleInitial;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String zipCode;
}
