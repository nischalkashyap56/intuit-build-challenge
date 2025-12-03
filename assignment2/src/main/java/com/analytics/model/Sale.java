package com.analytics.model;

import java.time.LocalDate;

// Correct formats of the data types are represented here
// Converted from Strings to these after cleaning the data and bucketizing into correct types
public record Sale(
        long transactionId,
        LocalDate date,
        String category,
        long productId,
        String product,
        String region,
        long paymentId,
        String paymentMethod,
        int quantity,
        double unitPrice
) {
    public double totalAmount() {
        return quantity * unitPrice;
    }
}
