package com.example.banktransaction.vo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Getter
@Setter
public class CustomerNameUpdate extends CustomerUpdate {

    private final String firstName;
    private final String middleName;
    private final String lastName;

    public CustomerNameUpdate(Long customerId, String firstName, String middleName, String lastName) {
        super(customerId);
        this.firstName = StringUtils.hasText(firstName) ? firstName : null;
        this.middleName = StringUtils.hasText(middleName) ? middleName : null;
        this.lastName = StringUtils.hasText(lastName) ? lastName : null;
    }
}
