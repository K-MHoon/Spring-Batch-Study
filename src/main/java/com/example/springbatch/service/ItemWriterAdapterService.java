package com.example.springbatch.service;

public class ItemWriterAdapterService<T> {

    public void customWrite(T item) {
        System.out.println(item);
    }
}
