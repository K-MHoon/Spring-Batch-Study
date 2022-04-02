package com.example.study.callback;

import com.example.study.dto.WriteCustomer;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

@Component
@Aspect
public class CustomerRecordCountFooterCallback implements FlatFileFooterCallback {

    private int itemsWrittenInCurrentFile = 0;

    @Override
    public void writeFooter(Writer writer) throws IOException {
        writer.write("This file contains " + itemsWrittenInCurrentFile + " items");
    }

    @Before("execution(* org.springframework.batch.item.support.AbstractFileItemWriter.open(..))")
    public void resetCounter() {
        this.itemsWrittenInCurrentFile = 0;
    }

    @Before("execution(* org.springframework.batch.item.support.AbstractFileItemWriter.write(..))")
    public void beforeWrite(JoinPoint joinPoint) {
        List<WriteCustomer> items = (List<WriteCustomer>) joinPoint.getArgs()[0];
        this.itemsWrittenInCurrentFile += items.size();
    }
}
