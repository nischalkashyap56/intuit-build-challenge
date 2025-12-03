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
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AnalyticsService {

    private final ImputationService imputer = new ImputationService();
    private final DataIngestionService parser = new DataIngestionService();

    public void processSalesData(Path csvPath) throws IOException {
        System.out.println("Phase 1: Analyzing data distribution");
        GlobalStats stats = imputer.calculateStats(csvPath);

        System.out.println("Mean for price: " + String.format("%.2f", stats.meanPrice()));
        System.out.println("Mode for category: " + stats.modeCategory());
        System.out.println("Mode for region: " + stats.modeRegion());

        System.out.println("Phase 2: Processing stream and imputing missing values");

        try (Stream<String> lines = Files.lines(csvPath).skip(1)) {

            List<Sale> validSales = lines
                    .map(this::tryParse)                  // Safe parsing wrapper
                    .filter(Objects::nonNull)             // Remove parsed failures
                    .map(raw -> tryImpute(raw, stats))    // Safe imputation wrapper
                    .filter(Objects::nonNull)             // Remove validation failures
                    .toList();

            runAnalytics(validSales);
        }
    }

    // Stream won't stop while parsing and continues by logging the errors
    private RawSale tryParse(String line) {
        try {
            return parser.parseLine(line);
        } catch (CsvParsingException e) {
            // Log the exception and continue
            AnalyticsLogger.logIngestionError(line, e.getMessage());
            return null;
        }
    }

    // Exception handling done with custom exception
    private Sale tryImpute(RawSale raw, GlobalStats stats) {
        try {
            return imputer.imputeAndMap(raw, stats);
        } catch (DataValidationException e) {
            // LOG the exception and continue
            AnalyticsLogger.logIngestionError(raw.toString(), e.getMessage());
            return null;
        }
    }

    private void runAnalytics(List<Sale> sales) {
        System.out.println("\n---- SALES ANALYTICS REPORT ----\n");

        // 1. Total Revenue per Region
        System.out.println("1. Total Revenue by Region:");
        Map<String, Double> revenueByRegion = sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::region,
                        Collectors.summingDouble(Sale::totalAmount)
                ));
        revenueByRegion.forEach((k, v) -> System.out.printf("%-2s : Rs. %,.2f%n", k, v));

        // 2. Monthly Revenue Trend
        System.out.println("\n2. Monthly Revenue Trends:");
        Map<String, Double> monthlySales = sales.stream()
                .collect(Collectors.groupingBy(
                        s -> s.date().getYear() + "-" + String.format("%02d", s.date().getMonthValue()),
                        Collectors.summingDouble(Sale::totalAmount)
                ));
        monthlySales.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.printf("%-2s : Rs. %,.2f%n", e.getKey(), e.getValue()));

        // 3. Top Selling Products
        System.out.println("\n3. Top 3 Selling Products:");
        sales.stream()
                .collect(Collectors.groupingBy(Sale::product, Collectors.summingInt(Sale::quantity)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(e -> System.out.printf("%-2s : %d units%n", e.getKey(), e.getValue()));

        // 4. Payment Method Distribution
        System.out.println("\n4. Payment Method Usage:");
        Map<String, Long> paymentStats = sales.stream()
                .collect(Collectors.groupingBy(Sale::paymentMethod, Collectors.counting()));
        paymentStats.forEach((k, v) -> System.out.printf("%-2s : %d transactions%n", k, v));

        // 5. Avg Unit Price by Category
        System.out.println("\n5. Avg Unit Price by Category:");
        sales.stream()
                .collect(Collectors.groupingBy(Sale::category, Collectors.averagingDouble(Sale::unitPrice)))
                .forEach((k, v) -> System.out.printf("%-2s : Rs. %,.2f%n", k, v));

        // ==================================================================================
        // NEW ADVANCED ANALYTICS (A, B, C)
        // ==================================================================================

        // 6. Option A: Regional Market Share (Nested Grouping)
        System.out.println("\n6. Regional Market Share (Top category per region):");
        // Logic: Group by Region -> Then Group by Category -> Sum Revenue
        Map<String, Map<String, Double>> regionalCategoryStats = sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::region,
                        Collectors.groupingBy(Sale::category, Collectors.summingDouble(Sale::totalAmount))
                ));

        regionalCategoryStats.forEach((region, catMap) -> {
            // Find the category with the highest revenue in this region
            var topEntry = catMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue());

            topEntry.ifPresent(e ->
                    System.out.printf("%-2s : Top Category is %-12s (Rs. %,.2f)%n", region, e.getKey(), e.getValue()));
        });

        // 7. Option B: Transaction Size Bucketing (Custom Classifier)
        System.out.println("\n7. Transaction Value Distribution (histograms):");
        Map<String, Long> valueBuckets = sales.stream()
                .collect(Collectors.groupingBy(
                        s -> {
                            double amt = s.totalAmount();
                            if (amt < 50.00) return "Low Value (<Rs. 50)";
                            else if (amt <= 150.00) return "Mid Value (Rs. 50-Rs. 150)";
                            else return "High Value (>Rs. 150)";
                        },
                        Collectors.counting()
                ));

        // Custom sort order for display
        Stream.of("Low Value (<Rs. 50)", "Mid Value (Rs. 50-Rs. 150)", "High Value (>Rs. 150)")
                .filter(valueBuckets::containsKey)
                .forEach(bucket -> System.out.printf("%-2s : %d transactions%n", bucket, valueBuckets.get(bucket)));


        // 8. Option C: Day-of-Week Heatmap (Time Series Analysis)
        System.out.println("\n8. Day-of-Week Profitability (heatmap):");
        Map<DayOfWeek, Double> dayStats = sales.stream()
                .collect(Collectors.groupingBy(
                        s -> s.date().getDayOfWeek(),
                        Collectors.averagingDouble(Sale::totalAmount)
                ));

        dayStats.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // Sort Mon -> Sun
                .forEach(e -> System.out.printf("%-2s : Avg Order Value Rs. %,.2f%n", e.getKey(), e.getValue()));
    }
}