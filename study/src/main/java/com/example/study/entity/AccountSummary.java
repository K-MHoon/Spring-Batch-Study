package com.example.study.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "tbl_account_summary")
public class AccountSummary {

    @Id @GeneratedValue
    @Column
    private Long id;

    @Column
    private String accountNumber;

    @Column
    private Double currentBalance;

}
