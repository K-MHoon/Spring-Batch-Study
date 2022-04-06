package com.example.banktransaction.classifier;

import com.example.banktransaction.vo.CustomerAddressUpdate;
import com.example.banktransaction.vo.CustomerContactUpdate;
import com.example.banktransaction.vo.CustomerNameUpdate;
import com.example.banktransaction.vo.CustomerUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.classify.Classifier;

@RequiredArgsConstructor
public class CustomerUpdateClassifier implements Classifier<CustomerUpdate, ItemWriter<? super CustomerUpdate>> {

    private final JdbcBatchItemWriter<CustomerUpdate> recordType1ItemWriter;
    private final JdbcBatchItemWriter<CustomerUpdate> recordType2ItemWriter;
    private final JdbcBatchItemWriter<CustomerUpdate> recordType3ItemWriter;

    @Override
    public ItemWriter<? super CustomerUpdate> classify(CustomerUpdate classifiable) {

        if(classifiable instanceof CustomerNameUpdate) {
            return recordType1ItemWriter;
        }
        else if(classifiable instanceof CustomerAddressUpdate) {
            return recordType2ItemWriter;
        }
        else if(classifiable instanceof CustomerContactUpdate) {
            return recordType3ItemWriter;
        }
        else {
            throw new IllegalArgumentException("Invalid type: " +
                    classifiable.getClass().getCanonicalName());
        }
    }
}
