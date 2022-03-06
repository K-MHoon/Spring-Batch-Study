package com.example.study.processor;

import com.example.study.dto.ProcessorInfo;
import org.springframework.batch.item.ItemProcessor;

public class ClassifierItemProcessor3 implements ItemProcessor<ProcessorInfo, ProcessorInfo> {

    @Override
    public ProcessorInfo process(ProcessorInfo item) throws Exception {
        System.out.println("ClassifierItemProcessor3.process");
        return item;
    }
}
