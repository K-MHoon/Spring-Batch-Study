package com.example.springbatch.mapper;

import com.example.springbatch.dto.Customer2;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class CustomerFieldSetMapperByNames implements FieldSetMapper<Customer2> {

    @Override
    public Customer2 mapFieldSet(FieldSet fieldSet) throws BindException {

        if(fieldSet == null) {
            return null;
        }

        Customer2 customer = new Customer2();
        customer.setName(fieldSet.readString("name"));
        customer.setAge(fieldSet.readInt("age"));
        customer.setYear(fieldSet.readString("year"));

        return customer;
    }
}
