package com.analytics.exception;

public class SalesAnalyticsException extends RuntimeException {
    public SalesAnalyticsException(String message) {
        super(message);
    }

    public SalesAnalyticsException(String message, Throwable cause) {
        super(message, cause);
    }
}
