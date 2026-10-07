package com.finance;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles saving/loading transactions to a plain CSV file on disk.
 * Demonstrates File Handling + Exception Handling (try-with-resources,
 * checked IOException propagated as a friendly message to the caller).
 */
public class FileHandler {

    /** Writes every transaction currently in the manager to the given file (overwrites it). */
    public static void save(FinanceManager manager, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("id,type,amount,category,description,date");
            for (Transaction t : manager.getAllTransactions()) {
                writer.println(t.toCsvLine());
            }
        }
    }

    /**
     * Loads transactions from the given CSV file into the manager, replacing
     * whatever was already there. Lines that fail to parse are skipped and
     * reported back to the caller rather than aborting the whole load.
     */
    public static LoadResult load(FinanceManager manager, String filePath) throws IOException {
        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }

        manager.clearAll();
        int loaded = 0;
        int skipped = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    Transaction t = Transaction.fromCsvLine(line);
                    manager.loadTransactionDirect(t);
                    loaded++;
                } catch (InvalidTransactionException e) {
                    skipped++;
                }
            }
        }
        return new LoadResult(loaded, skipped);
    }

    /** Simple result holder so Main can report what happened after a load. */
    public static class LoadResult {
        public final int loadedCount;
        public final int skippedCount;
        public LoadResult(int loadedCount, int skippedCount) {
            this.loadedCount = loadedCount;
            this.skippedCount = skippedCount;
        }
    }
}
