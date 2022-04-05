package com.example.banktransaction.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerUpdate {
    protected final Long customerId;

    public CustomerUpdate(Long customerId) {
        this.customerId = customerId;
    }
}
