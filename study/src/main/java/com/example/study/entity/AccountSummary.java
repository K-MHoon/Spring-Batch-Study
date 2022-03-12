package com.example.study.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Data
@Entity
@Table(name = "tbl_account_summary")
public class AccountSummary {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String accountNumber;

    @Column
    private Double currentBalance;

}
