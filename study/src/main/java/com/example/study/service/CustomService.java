package com.example.study.service;

public class CustomService<T> {

    private int cnt = 0;

    public T customRead() {
        return (T)("item" + cnt++);
    }
}
