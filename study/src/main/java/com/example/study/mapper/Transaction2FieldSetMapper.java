package com.example.study.mapper;

import com.example.study.dto.Transaction2;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class Transaction2FieldSetMapper implements FieldSetMapper<Transaction2> {
    @Override
    public Transaction2 mapFieldSet(FieldSet fieldSet) throws BindException {
        Transaction2 trans = new Transaction2();

        trans.setAccountNumber(fieldSet.readString("accountNumber"));
        trans.setAmount(fieldSet.readDouble("amount"));
        trans.setTransactionDate(fieldSet.readDate("transactionDate", "yyyy-MM-dd HH:mm:ss"));
        return trans;
    }
}
