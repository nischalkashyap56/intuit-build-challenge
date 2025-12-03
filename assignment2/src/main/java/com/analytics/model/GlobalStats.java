package com.analytics.model;

// Two pass approach is taken here to handle missing data with imputation if required
// This is for the first pass it stores the average global stat metrics to be able to impute in second pass is needed
public record GlobalStats(
        double meanPrice,
        String modeCategory,
        String modeRegion,
        String modePaymentMethod,
        int modeQuantity
) {}
