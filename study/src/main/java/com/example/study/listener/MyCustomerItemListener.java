package com.example.study.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.annotation.OnReadError;
import org.springframework.batch.item.file.FlatFileParseException;

@Slf4j
public class MyCustomerItemListener {

    @OnReadError
    public void onReadError(Exception e) {
        if(e instanceof FlatFileParseException) {
            FlatFileParseException ffpe = (FlatFileParseException) e;

            StringBuilder errMsg = new StringBuilder();
            errMsg.append("An error occurred while processing the "
                    + ffpe.getLineNumber() +
                    " line of the file. Below was the faulty input.\n");
            errMsg.append(ffpe.getInput() + "\n");
            log.error(errMsg.toString(), ffpe);
        } else {
            log.error("An error has occurred", e);
        }
    }
}
