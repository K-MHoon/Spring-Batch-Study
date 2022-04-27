package com.example.banktransaction.validator;

import com.example.banktransaction.vo.CustomerUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@ExtendWith(SpringExtension.class)
@JdbcTest // 인메모리 DB 생성, 스프링 부트가 일반적으로 사용하는 데이터를 초기화 스크립트 실행
class CustomerItemValidatorIntegrationTest {

    @Autowired
    private DataSource dataSource;

    private CustomerItemValidator customerItemValidator;

    @BeforeEach
    public void setup() {
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(this.dataSource);
        this.customerItemValidator = new CustomerItemValidator(template);
    }

    @Test
    public void testNoCustomers() {
        CustomerUpdate customerUpdate = new CustomerUpdate(-5L);

        ValidationException exception = assertThrows(ValidationException.class, () -> this.customerItemValidator.validate(customerUpdate));

        assertEquals("Bank Customer id -5 was not able to be found", exception.getMessage());
    }

    @Test
    public void testCustomers() {
        CustomerUpdate customerUpdate = new CustomerUpdate(5L);
        this.customerItemValidator.validate(customerUpdate);
    }
}