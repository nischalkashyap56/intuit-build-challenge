package com.analytics;

import com.analytics.exception.CsvParsingException;
import com.analytics.exception.DataValidationException;
import com.analytics.model.GlobalStats;
import com.analytics.model.RawSale;
import com.analytics.model.Sale;
import com.analytics.service.DataIngestionService;
import com.analytics.service.ImputationService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SalesAnalyticsTest {

    private final DataIngestionService parser = new DataIngestionService();
    private final ImputationService imputer = new ImputationService();

    @Test
    void testParsingStructure() {
        String validLine = "101,2023-01-01,Electronics,500,Laptop,North,900,Card,1,1200.50";
        RawSale result = parser.parseLine(validLine);
        assertNotNull(result);
        assertEquals("101", result.transactionId());
    }

    @Test
    void testParsingMissingColumnsThrowsException() {
        String invalidLine = "101,2023-01-01,Electronics,500";
        // Check custom exception is thrown
        assertThrows(CsvParsingException.class, () -> {
            parser.parseLine(invalidLine);
        }, "Should throw CsvParsingException for incomplete columns");
    }

    @Test
    void testImputationOfMissingPrice() {
        GlobalStats stats = new GlobalStats(500.0, "Tech", "West", "Cash", 1);
        RawSale dirty = new RawSale(
                "102", "2023-05-20", "Tech", "55", "Mouse", "West", "99", "Cash", "1", ""
        );

        Sale clean = imputer.imputeAndMap(dirty, stats);
        assertNotNull(clean);
        assertEquals(500.0, clean.unitPrice(), 0.001);
    }

    @Test
    void testDateValidationThrowsException() {
        GlobalStats stats = new GlobalStats(100.0, "A", "B", "C", 1);
        RawSale badDate = new RawSale(
                "104", "INVALID-DATE", "Cat", "1", "Prod", "Reg", "1", "Pay", "1", "10.0"
        );

        // Check custom exception is thrown
        assertThrows(DataValidationException.class, () -> {
            imputer.imputeAndMap(badDate, stats);
        }, "Should throw DataValidationException for invalid ISO date");
    }
}
