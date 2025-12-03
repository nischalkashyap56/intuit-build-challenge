package com.analytics;

import com.analytics.service.AnalyticsService;
import com.analytics.exception.SalesAnalyticsException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        System.out.println("Sales Analytics Service on CSV --- ");

        Path csvPath = Paths.get("src/main/resources/sales_data.csv");

        if (!Files.exists(csvPath)) {
            System.err.println("File Not Found: 'sales_data.csv' not found at " + csvPath.toAbsolutePath() + " please check file path");
            System.exit(1);
        }

        try {
            AnalyticsService engine = new AnalyticsService();
            engine.processSalesData(csvPath);
            System.out.println("Processing complete! For more detailed logs on ingestion errors and cleaning, check 'DataIngestionErrors.log' and 'DataCleaning.log' files");
        } catch (IOException e) {
            System.err.println("I/O ERROR: " + e.getMessage());
            e.printStackTrace();
        } catch (SalesAnalyticsException e) {
            System.err.println("APP ERROR: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("UNEXPECTED RUNTIME ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}