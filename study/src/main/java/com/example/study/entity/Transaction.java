package com.example.study.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "tbl_transaction")
@XmlRootElement(name = "transaction")
public class Transaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private Date timestamp;

    @Column
    private String accountNumber;

    @Column
    private double amount;

    @ManyToOne
    @JoinColumn(name = "account_summary_id")
    private AccountSummary accountSummary;
}
