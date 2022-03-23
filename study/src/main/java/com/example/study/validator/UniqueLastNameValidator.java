package com.example.study.validator;

import com.example.study.dto.MyCustomer2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamSupport;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.batch.item.validator.Validator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class UniqueLastNameValidator extends ItemStreamSupport implements Validator<MyCustomer2> {

    private Set<String> lastNames = new HashSet<>();

    @Override
    public void validate(MyCustomer2 value) throws ValidationException {
        if(lastNames.contains(value.getLastName())) {
            throw new ValidationException("Duplicate last name was found: " +
                    value.getLastName());
        }
        this.lastNames.add(value.getLastName());
    }

    @Override
    public void open(ExecutionContext executionContext) {
        String lastNames = getExecutionContextKey("lastNames");
        // lastNames 필드가 이전 Execution에 저장돼 있는지 확인한다.
        // 있으면, 스탭 시작 전에 원복한다.
        if(executionContext.containsKey(lastNames)) {
            this.lastNames = (Set<String>) executionContext.get(lastNames);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) {
        Iterator<String> itr = lastNames.iterator();
        Set<String> copiedLastNames = new HashSet<>();
        while(itr.hasNext()) {
            copiedLastNames.add(itr.next());
        }
        executionContext.put(getExecutionContextKey("lastNames"), copiedLastNames);
    }
}
