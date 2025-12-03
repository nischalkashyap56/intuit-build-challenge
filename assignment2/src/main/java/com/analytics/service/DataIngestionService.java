package com.analytics.service;

import com.analytics.exception.CsvParsingException;
import com.analytics.model.RawSale;

public class DataIngestionService {

    private static final String CSV_DELIMITER = ",";

    // Throws custom CsvParsingException here if there is any errors here
    public RawSale parseLine(String line) {
        if (line == null || line.isBlank()) {
            // We throw exception to be handled by the caller/logger
            throw new CsvParsingException("CsvParsingError: Line here is either null or empty and couldn't be parsed!");
        }

        String[] parts = line.split(CSV_DELIMITER, -1);

        // We expect 10 columns based on the schema
        if (parts.length < 10) {
            throw new CsvParsingException("CsvParsingError: Based on sample CSV schema generated, 10 columns are required: Expected 10 but found : " + parts.length);
        }

        return new RawSale(
                parts[0].trim(), // Transaction ID
                parts[1].trim(), // Date
                parts[2].trim(), // Category
                parts[3].trim(), // Product ID
                parts[4].trim(), // Product
                parts[5].trim(), // Region
                parts[6].trim(), // Payment ID
                parts[7].trim(), // Payment Method
                parts[8].trim(), // Quantity
                parts[9].trim()  // Unit Price
        );
    }
}
