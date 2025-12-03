package com.analytics.service;

import com.analytics.exception.CsvParsingException;
import com.analytics.exception.DataValidationException;
import com.analytics.model.GlobalStats;
import com.analytics.model.RawSale;
import com.analytics.model.Sale;
import com.analytics.util.AnalyticsLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Stream;

public class ImputationService {

    private final DataIngestionService parser = new DataIngestionService();

    // Calculates global mean and mode for imputation in one pass
    // Handles errros by simply skipping over them
    public GlobalStats calculateStats(Path csvPath) throws IOException {
        // Skip the header of CSV file
        try (Stream<String> lines = Files.lines(csvPath).skip(1)) {

            DoubleSummaryStatistics priceStats = new DoubleSummaryStatistics();
            Map<String, Long> categoryFreq = new HashMap<>();
            Map<String, Long> regionFreq = new HashMap<>();

            lines.forEach(line -> {
                try {
                    RawSale raw = parser.parseLine(line);
                    // Price
                    if (isValidDouble(raw.unitPrice())) {
                        priceStats.accept(Double.parseDouble(raw.unitPrice()));
                    }
                    
                    // Category
                    if (!raw.category().isBlank()) {
                        categoryFreq.merge(raw.category(), 1L, Long::sum);
                    }
                    
                    // Region
                    if (!raw.region().isBlank()) {
                        regionFreq.merge(raw.region(), 1L, Long::sum);
                    }
                } catch (CsvParsingException e) {
                    // No need to log here because Pass 2 will log to the relevant log file
                }
            });

            double meanPrice = priceStats.getCount() > 0 ? priceStats.getAverage() : 0.0;
            String modeCategory = getMode(categoryFreq);
            String modeRegion = getMode(regionFreq);

            return new GlobalStats(meanPrice, modeCategory, modeRegion, "Cash", 1);
        }
    }
    

    public Sale imputeAndMap(RawSale raw, GlobalStats stats) {

        long txId;
        try {
            txId = Long.parseLong(raw.transactionId());
        } catch (NumberFormatException e) {
            throw new DataValidationException("Invalid Transaction ID: " + raw.transactionId());
        }

        LocalDate date;
        try {
            date = LocalDate.parse(raw.date());
        } catch (DateTimeParseException e) {
            throw new DataValidationException("Invalid ISO Date format: " + raw.date());
        }

        // Impute categories
        String finalCategory = raw.category();
        if (finalCategory.isBlank()) {
            finalCategory = stats.modeCategory();
            AnalyticsLogger.logDataCleaning(String.valueOf(txId), "Category", "MISSING", finalCategory);
        }

        // Impute regions
        String finalRegion = raw.region();
        if (finalRegion.isBlank()) {
            finalRegion = stats.modeRegion();
            AnalyticsLogger.logDataCleaning(String.valueOf(txId), "Region", "MISSING", finalRegion);
        }

        // Impute prices
        double finalPrice;
        if (isValidDouble(raw.unitPrice())) {
            finalPrice = Double.parseDouble(raw.unitPrice());
        } else {
            finalPrice = stats.meanPrice();
            AnalyticsLogger.logDataCleaning(String.valueOf(txId), "UnitPrice", raw.unitPrice(), String.format("%.2f", finalPrice));
        }

        // Impute quantity
        int finalQty = 1;
        try {
            finalQty = Integer.parseInt(raw.quantity());
        } catch (NumberFormatException e) {
            AnalyticsLogger.logDataCleaning(String.valueOf(txId), "Quantity", raw.quantity(), "1 (Default)");
        }

        long prodId = parseLongSafe(raw.productId(), -1);
        long payId = parseLongSafe(raw.paymentId(), -1);

        return new Sale(
                txId, date, finalCategory, prodId, raw.product(),
                finalRegion, payId, raw.paymentMethod(), finalQty, finalPrice
        );
    }

    // Simple validity check for double for cost column
    private boolean isValidDouble(String str) {
        if (str == null || str.isBlank()) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private long parseLongSafe(String str, long defaultVal) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private String getMode(Map<String, Long> freqMap) {
        return freqMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");
    }
}