package com.example.study.repository;

import com.example.study.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionRepository, Long> {
    List<Transaction> findByAccountNumber(String accountNumber);
}
