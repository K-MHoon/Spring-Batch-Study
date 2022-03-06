package com.example.study.processor;

import com.example.study.exception.RetryableException;
import org.springframework.batch.item.ItemProcessor;

public class RetryCustomItemProcessor implements ItemProcessor<Integer, String> {

    int count = 0;

    @Override
    public String process(Integer item) throws Exception {
        if(count < 2) {
            if(count %2 == 0) {
                count++;
            } else if(count % 2 == 1) {
                count++;
                throw new RetryableException("failed");
            }
        }

        return String.valueOf(item);
    }
}
