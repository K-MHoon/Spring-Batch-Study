package com.example.springbatch.reader;

import com.example.springbatch.dto.Customer;
import org.springframework.batch.item.*;

import java.util.List;

public class CustomItemStreamReader implements ItemStreamReader<Customer> {

    private final List<Customer> items;
    private int index = -1;
    // 재시작 여부
    private boolean restart = false;

    public CustomItemStreamReader(List<Customer> items) {
        this.items = items;
        this.index = 0;
    }

    @Override
    public Customer read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        Customer item = null;

        if(this.index < this.items.size()) {
            item = this.items.get(index++);
        }

        if(this.index == 6 && !restart) {
            throw new RuntimeException("Restart is required");
        }

        return item;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        // DB에 저장한 index에 해당한 키가 있는지 확인하고, 있으면 해당 인덱스로 설정하고 재시작 할 수 있도록 초기화 작업.
        if(executionContext.containsKey("index")) {
            index = executionContext.getInt("index");
            this.restart = true;

        // 처음이면 0으로 초기화
        } else {
            index = 0;
            executionContext.put("index", index);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.put("index", index);
    }

    @Override
    public void close() throws ItemStreamException {
        System.out.println("closed");
    }
}
