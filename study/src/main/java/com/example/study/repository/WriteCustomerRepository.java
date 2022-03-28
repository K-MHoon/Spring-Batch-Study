package com.example.study.repository;

import com.example.study.dto.WriteCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WriteCustomerRepository extends JpaRepository<WriteCustomer, Long> {
}
