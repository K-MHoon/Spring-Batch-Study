package com.example.study.processor;

import com.example.study.dto.ProcessorInfo;
import org.springframework.batch.item.ItemProcessor;

public class ClassifierItemProcessor1 implements ItemProcessor<ProcessorInfo, ProcessorInfo> {

    @Override
    public ProcessorInfo process(ProcessorInfo item) throws Exception {

        System.out.println("ClassifierItemProcessor1.process");

        return item;
    }
}
