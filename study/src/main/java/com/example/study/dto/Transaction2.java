package com.example.study.dto;

import lombok.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class Transaction2 {

    private String accountNumber;
    private Date transactionDate;
    private Double amount;
    private DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
}
