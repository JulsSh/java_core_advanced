package com.example.parse;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class FileFinder {
    public static class Result {
        public final List<Path> json = new ArrayList<>();
        public final List<Path> csv = new ArrayList<>();
    }

    public Result find(Path root) throws IOException {
        Result r = new Result();
        try (var stream = Files.walk(root)) {
            stream.filter(Files::isRegularFile).forEach(p -> {
                String name = p.getFileName().toString().toLowerCase();
                if (name.endsWith(".json")) {r.json.add(p);}
                else if (name.endsWith(".csv")) {r.csv.add(p);}
            });
        }
        return r;
    }
}
