package com.example.study.repository;

import com.example.study.dto.MyCustomer5;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyCustomerRepository extends JpaRepository<MyCustomer5, Long> {
    Page<MyCustomer5> findByCity(String city, Pageable pageable);
}
