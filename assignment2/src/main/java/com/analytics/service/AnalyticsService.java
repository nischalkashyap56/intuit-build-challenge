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

        System.out.println("Mean price: " + String.format("%.2f", stats.meanPrice()));
        System.out.println("Mode category: " + stats.modeCategory());
        System.out.println("Mode region: " + stats.modeRegion());
        System.out.println();
        System.out.println("Processing stream & imputing missing values ");

        try (Stream<String> lines = Files.lines(csvPath).skip(1)) {

            // Stream usage and parsing lines
            List<Sale> validSales = lines
                    .map(this::tryParse)
                    .filter(Objects::nonNull)
                    .map(raw -> tryImpute(raw, stats))
                    .filter(Objects::nonNull)
                    .toList();

            runAnalytics(validSales);
        }

        System.out.println("Finished stream processing & imputing missing values ");
    }

    // In case Stream might stop if there is a bad line, this prevents it by logging exception and continuing
    private RawSale tryParse(String line) {
        try {
            return parser.parseLine(line);
        } catch (CsvParsingException e) {
            AnalyticsLogger.logIngestionError(line, e.getMessage());
            return null;
        }
    }

    // Exception handling for validations
    private Sale tryImpute(RawSale raw, GlobalStats stats) {
        try {
            return imputer.imputeAndMap(raw, stats);
        } catch (DataValidationException e) {
            // Log the exception in the respective file and we continue
            AnalyticsLogger.logIngestionError(raw.toString(), e.getMessage());
            return null;
        }
    }

    private void runAnalytics(List<Sale> sales) {
        System.out.println("\n---- SALES ANALYTICS REPORT ----\n");
        System.out.println("1. Total Revenue by Region:");
        Map<String, Double> revenueByRegion = sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::region,
                        Collectors.summingDouble(Sale::totalAmount)
                ));
        revenueByRegion.forEach((k, v) -> System.out.printf("%-2s : Rs. %,.2f%n", k, v));


        System.out.println("\n2. Monthly Revenue Trends:");
        Map<String, Double> monthlySales = sales.stream()
                .collect(Collectors.groupingBy(
                        s -> s.date().getYear() + "-" + String.format("%02d", s.date().getMonthValue()),
                        Collectors.summingDouble(Sale::totalAmount)
                ));
        monthlySales.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.printf("%-2s : Rs. %,.2f%n", e.getKey(), e.getValue()));


        System.out.println("\n3. Top 3 Selling Products:");
        sales.stream()
                .collect(Collectors.groupingBy(Sale::product, Collectors.summingInt(Sale::quantity)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(e -> System.out.printf("%-2s : %d units%n", e.getKey(), e.getValue()));


        System.out.println("\n4. Payment Method Usage:");
        Map<String, Long> paymentStats = sales.stream()
                .collect(Collectors.groupingBy(Sale::paymentMethod, Collectors.counting()));
        paymentStats.forEach((k, v) -> System.out.printf("%-2s : %d transactions%n", k, v));


        System.out.println("\n5. Avg Unit Price by Category:");
        sales.stream()
                .collect(Collectors.groupingBy(Sale::category, Collectors.averagingDouble(Sale::unitPrice)))
                .forEach((k, v) -> System.out.printf("%-2s : Rs. %,.2f%n", k, v));

        System.out.println();
    }
}