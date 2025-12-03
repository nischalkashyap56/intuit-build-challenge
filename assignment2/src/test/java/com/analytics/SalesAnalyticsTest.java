package com.analytics;

import com.analytics.exception.CsvParsingException;
import com.analytics.exception.DataValidationException;
import com.analytics.model.GlobalStats;
import com.analytics.model.RawSale;
import com.analytics.model.Sale;
import com.analytics.service.DataIngestionService;
import com.analytics.service.ImputationService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        assertThrows(DataValidationException.class, () -> {
            imputer.imputeAndMap(badDate, stats);
        }, "Should throw DataValidationException for invalid ISO date");
    }

    @Nested
    class AdvancedAnalyticsLogicTest {

        // Helper to create valid sales
        private Sale createSale(String region, String category, double price, int qty, String dateStr) {
            return new Sale(
                    1L, LocalDate.parse(dateStr), category, 1L, "Prod", region,
                    1L, "Card", qty, price
            );
        }

        private final List<Sale> mockSales = List.of(
                createSale("North", "Electronics", 1000.0, 1, "2023-01-01"), // High Value
                createSale("North", "Clothing", 20.0, 1, "2023-01-01"),      // Low Value
                createSale("South", "Clothing", 100.0, 2, "2023-01-02"),     // Mid Value ($200 total -> High)
                createSale("South", "Clothing", 40.0, 1, "2023-01-03")       // Low Value
        );

        @Test
        void testOptionA_RegionalMarketShare() {
            Map<String, Map<String, Double>> stats = mockSales.stream()
                    .collect(Collectors.groupingBy(
                            Sale::region,
                            Collectors.groupingBy(Sale::category, Collectors.summingDouble(Sale::totalAmount))
                    ));

            assertEquals(1000.0, stats.get("North").get("Electronics"));
            assertEquals(20.0, stats.get("North").get("Clothing"));

            assertEquals(240.0, stats.get("South").get("Clothing"));
        }

        @ParameterizedTest(name = "Val: {0}, Qty: {1} -> Expected Bucket: {2}")
        @CsvSource({
                "40.0, 1, Low Value (<$50)",
                "49.99, 1, Low Value (<$50)",
                "50.0, 1, Mid Value ($50-$150)",
                "150.0, 1, Mid Value ($50-$150)",
                "150.01, 1, High Value (>$150)",
                "100.0, 2, High Value (>$150)" // 100 * 2 = 200
        })
        void testOptionB_BucketingLogic(double price, int qty, String expectedBucket) {
            Sale s = createSale("North", "Test", price, qty, "2023-01-01");

            String actualBucket;
            double amt = s.totalAmount();

            if (amt < 50.00) actualBucket = "Low Value (<$50)";
            else if (amt <= 150.00) actualBucket = "Mid Value ($50-$150)";
            else actualBucket = "High Value (>$150)";

            assertEquals(expectedBucket, actualBucket);
        }

        @Test
        void testOptionC_DayOfWeekHeatmap() {
            List<Sale> timeSales = List.of(
                    createSale("N", "C", 100.0, 1, "2023-01-02"), // Mon
                    createSale("N", "C", 200.0, 1, "2023-01-02"), // Mon
                    createSale("N", "C", 50.0, 1, "2023-01-03")   // Tue
            );

            Map<java.time.DayOfWeek, Double> dayStats = timeSales.stream()
                    .collect(Collectors.groupingBy(
                            s -> s.date().getDayOfWeek(),
                            Collectors.averagingDouble(Sale::totalAmount)
                    ));

            assertEquals(150.0, dayStats.get(java.time.DayOfWeek.MONDAY), "Monday average should be (100+200)/2");
            assertEquals(50.0, dayStats.get(java.time.DayOfWeek.TUESDAY), "Tuesday average should be 50");
        }
    }
}
