package com.example.banktransaction.validator;

import com.example.banktransaction.vo.CustomerUpdate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CustomerItemValidatorTest {

    @Mock
    private NamedParameterJdbcTemplate template;

    private CustomerItemValidator validator;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this); // 목 초기화 (사용자가 목 객체로 사용할 모든 객체를 초기화)
        this.validator = new CustomerItemValidator(this.template);
    }

    @Test
    public void testValidCustomer() {
        //given
        CustomerUpdate customer = new CustomerUpdate(5L);

        // when
        ArgumentCaptor<Map<String, Long>> parameterMap = ArgumentCaptor.forClass(Map.class);
        when(this.template.queryForObject(eq(CustomerItemValidator.FIND_CUSTOMER),
                parameterMap.capture(),
                eq(Long.class)))
                .thenReturn(2L);

        this.validator.validate(customer);

        // then
        assertEquals(5L, (long)parameterMap.getValue().get("id"));
    }

    @Test
    public void testInvalidCustomer() {
        //given
        CustomerUpdate customer = new CustomerUpdate(5L);

        // when
        ArgumentCaptor<Map<String, Long>> parameterMap = ArgumentCaptor.forClass(Map.class);
        when(this.template.queryForObject(eq(CustomerItemValidator.FIND_CUSTOMER),
                parameterMap.capture(),
                eq(Long.class)))
                .thenReturn(0L);

        Throwable exception =
                assertThrows(ValidationException.class, () -> this.validator.validate(customer));

        // then
        assertEquals("Bank Customer id 5 was not able to be found", exception.getMessage());
    }

}