package com.analytics.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class AnalyticsLogger {
    // Adds to the data ingestion error and data cleaning errors separate log files
    private static final Path ERROR_LOG = Paths.get("DataIngestionErrors.log");
    private static final Path CLEANING_LOG = Paths.get("DataCleaning.log");

    static {
        try {
            Files.writeString(ERROR_LOG, "--- INGESTION ERRORS LOG ---\nTimestamp: " + java.time.Instant.now() + "\n\n");
            Files.writeString(CLEANING_LOG, "--- DATA CLEANING LOG ---\nTimestamp: " + java.time.Instant.now() + "\n\n");
        } catch (IOException e) {
            System.err.println("[CRITICAL] Could not initialize log files.");
        }
    }

    public static void logIngestionError(String rawLine, String exceptionMsg) {
        String msg = String.format("[INGESTION ERROR LOG] Exception: %s | Line: %s", exceptionMsg, rawLine);
        append(ERROR_LOG, msg);
    }

    public static void logDataCleaning(String id, String field, String original, String imputed) {
        String msg = String.format("[CLEANING LOG] TxID: %-10s | Field: %-10s | Original: '%-8s' -> Imputed: %s",
                id, field, original, imputed);
        append(CLEANING_LOG, msg);
    }

    private static void append(Path path, String msg) {
        try {
            Files.writeString(path, msg + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Failed to write to log: " + e.getMessage());
        }
    }
}