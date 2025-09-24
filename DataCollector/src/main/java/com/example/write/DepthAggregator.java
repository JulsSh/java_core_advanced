package com.example.write;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepthAggregator {
    private final ObjectMapper mapper = new ObjectMapper();

    public Map<String, Double> readDepths(List<Path> jsonFiles) throws IOException {
        Map<String, Double> result = new HashMap<>();
        for (Path p : jsonFiles) {
            byte[] bytes = Files.readAllBytes(p);
            JsonNode root = mapper.readTree(bytes);
            if (!root.isArray()) {continue;}
            for (JsonNode n : root) {
                JsonNode nameN = n.get("station_name");
                JsonNode depthN = n.get("depth");
                if (nameN == null || depthN == null) {continue;}
                String name = nameN.asText().trim();
                Double d = parseDepth(depthN.asText());
                if (name.isEmpty() || d == null) {continue;}
                result.merge(name, d, (a, b) -> Math.abs(b) > Math.abs(a) ? b : a);
            }
        }
        return result;
    }

    private static Double parseDepth(String s) {
        if (s == null) {return null;}
        String norm = s.replace(",", ".").replaceAll("[^\\-0-9.]", "");
        if (norm.isBlank()) {return null;}
        try { return Double.parseDouble(norm); } catch (Exception e) { return null; }
    }
}