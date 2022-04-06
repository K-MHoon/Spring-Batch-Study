package com.example.banktransaction.validator;

import com.example.banktransaction.vo.CustomerUpdate;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Map;

@Component
public class CustomerItemValidator implements Validator<CustomerUpdate> {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String FIND_CUSTOMER =
            "SELECT COUNT(*) FROM bank_customer WHERE customer_id = :id";

    public CustomerItemValidator(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public void validate(CustomerUpdate value) throws ValidationException {
        Map<String, Long> parameterMap = Collections.singletonMap("id", value.getCustomerId());

        Long count = jdbcTemplate.queryForObject(FIND_CUSTOMER, parameterMap, Long.class);
        if(count == 0) {
            throw new ValidationException(
                    String.format("Bank Customer id %s was not able to be found",
                            value.getCustomerId()));
        }
    }
}
