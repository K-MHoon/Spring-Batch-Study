package com.example.study.repository;

import com.example.study.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("select t from Transaction t " +
            "inner join t.accountSummary a " +
            "where a.accountNumber = :accountNumber")
    List<Transaction> getTransactionsByAccountNumber(@Param("accountNumber") String accountNumber);
}
