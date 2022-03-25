package com.example.study.classifier;

import com.example.study.dto.MyCustomer2;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.classify.Classifier;

public class ZipCodeClassifier implements Classifier<MyCustomer2, ItemProcessor<MyCustomer2, MyCustomer2>> {

    private ItemProcessor<MyCustomer2, MyCustomer2> oddItemProcessor;
    private ItemProcessor<MyCustomer2, MyCustomer2> evenItemProcessor;

    public ZipCodeClassifier(ItemProcessor<MyCustomer2, MyCustomer2> oddItemProcessor, ItemProcessor<MyCustomer2, MyCustomer2> evenItemProcessor) {
        this.oddItemProcessor = oddItemProcessor;
        this.evenItemProcessor = evenItemProcessor;
    }

    @Override
    public ItemProcessor<MyCustomer2, MyCustomer2> classify(MyCustomer2 classifiable) {
        if(Integer.parseInt(classifiable.getZipCode())%2 == 0) {
            return evenItemProcessor;
        } else {
            return oddItemProcessor;
        }
    }
}
