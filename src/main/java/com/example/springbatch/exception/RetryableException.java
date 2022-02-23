package com.example.springbatch.exception;

public class RetryableException extends RuntimeException {

    public RetryableException(String message) {
        super(message);
    }

    public RetryableException() {
        super();
    }
}
