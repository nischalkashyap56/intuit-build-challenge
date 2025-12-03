package com.analytics.model;

// Reads raw lines from CSV (taken in as strings but then converted to actual data types)
public record RawSale(
        String transactionId,
        String date,
        String category,
        String productId,
        String product,
        String region,
        String paymentId,
        String paymentMethod,
        String quantity,
        String unitPrice
) {}
