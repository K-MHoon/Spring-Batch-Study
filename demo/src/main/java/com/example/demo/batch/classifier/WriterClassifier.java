package com.example.demo.batch.classifier;

import com.example.demo.batch.domain.ApiRequestVO;
import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

import java.util.HashMap;
import java.util.Map;

public class WriterClassifier<C, T> implements Classifier<C, T> {

    private Map<String, ItemWriter<ApiRequestVO>> writerMap = new HashMap<>();

    public void setWriterMap(Map<String, ItemWriter<ApiRequestVO>> processorMap) {
        this.writerMap = processorMap;
    }

    @Override
    public T classify(C c) {
        return (T)writerMap.get(((ApiRequestVO)c).getProductVO().getType());
    }
}
