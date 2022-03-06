package com.example.study.listener;

import org.springframework.batch.core.SkipListener;

public class CustomSkipListener implements SkipListener<Integer, String> {

    @Override
    public void onSkipInRead(Throwable t) {
        System.out.println(">> onSkipRead : " + t.getMessage());
    }

    @Override
    public void onSkipInWrite(String item, Throwable t) {
        System.out.println(">> onSkipWriter : " + item);
        System.out.println(">> onSkipWriter : " + t.getMessage());
    }

    @Override
    public void onSkipInProcess(Integer item, Throwable t) {
        System.out.println(">> onSkipProcess : " + item);
        System.out.println(">> onSkipProcess : " + t.getMessage());
    }
}
