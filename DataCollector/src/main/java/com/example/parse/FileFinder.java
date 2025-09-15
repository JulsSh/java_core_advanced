package com.example.parse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileFinder {
    private static final Logger LOG = LoggerFactory.getLogger(FileFinder.class);

    public static class FoundFiles {
        public final List<Path> jsonFiles = new ArrayList<>();
        public final List<Path> csvFiles = new ArrayList<>();
    }

    public FoundFiles findJsonAndCsv(Path root) throws IOException {
        FoundFiles found = new FoundFiles();
        Files.walk(root)
                .filter(Files::isRegularFile)
                .forEach(p -> {
                    String name = p.getFileName().toString().toLowerCase();
                    if (name.endsWith(".json")) {
                        found.jsonFiles.add(p);
                    } else if (name.endsWith(".csv")) {
                        found.csvFiles.add(p);
                    }
                });
        LOG.info("Found JSON: {}", found.jsonFiles.size());
        LOG.info("Found CSV: {}", found.csvFiles.size());
        return found;
    }
}
