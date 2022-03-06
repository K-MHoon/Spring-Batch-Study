package com.example.study.processor;

import com.example.study.dto.Customer4;
import com.example.study.dto.Customer5;
import org.modelmapper.ModelMapper;
import org.springframework.batch.item.ItemProcessor;

public class JpaItemWriteProcessor implements ItemProcessor<Customer4, Customer5> {

    ModelMapper modelMapper = new ModelMapper();

    @Override
    public Customer5 process(Customer4 item){
        return modelMapper.map(item, Customer5.class);
    }
}