package com.example.study.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "tbl_transaction")
public class Transaction {

    @Id @GeneratedValue
    @Column
    private Long id;

    @Column
    private String accountNumber;

    @Column
    private Date timestamp;

    @Column
    private double amount;
}
