package com.example.study.repository;

import com.example.study.entity.AccountSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountSummaryRepository extends JpaRepository<AccountSummary, Long> {
}
