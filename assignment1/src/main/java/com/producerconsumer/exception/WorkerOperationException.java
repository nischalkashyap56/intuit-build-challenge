package com.producerconsumer.exception;

public class WorkerOperationException extends RuntimeException {
    public WorkerOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}