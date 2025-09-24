package com.example.parse;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FileFinder {
    public static class Result {
        public final List<Path> json = new ArrayList<>();
        public final List<Path> csv = new ArrayList<>();
    }

    private static final Set<String> IGNORE_BASENAMES = Set.of("map.json", "stations.json");

    public Result find(Path root) throws IOException {
        if (!Files.exists(root)) {
            throw new NoSuchFileException("Нет такой папки: " + root.toAbsolutePath());
        }
        if (!Files.isDirectory(root)) {
            throw new NotDirectoryException("Ожидалась папка, но это не директория: " + root.toAbsolutePath());
        }

        Result r = new Result();
        try (var stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().startsWith(".")) // .DS_Store и т.п.
                    .filter(p -> !IGNORE_BASENAMES.contains(p.getFileName().toString().toLowerCase())) // <-- игнор
                    .forEach(p -> {
                        String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
                        if (name.endsWith(".json")) { r.json.add(p); }
                        else if (name.endsWith(".csv")) { r.csv.add(p); }
                    });
        }
        return r;
    }
}
