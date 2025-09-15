package com.example.parse;

import com.example.model.StationProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class JsonStationsParser {
    private final ObjectMapper mapper = new ObjectMapper();

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("d.M.yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("ru"))
    );

    public List<StationProperties> parse(Path jsonFile) throws IOException {
        byte[] bytes = Files.readAllBytes(jsonFile);
        JsonNode root = mapper.readTree(bytes);

        List<StationProperties> result = new ArrayList<>();

        if (root.isArray()) {
            for (JsonNode n : root) {
                result.add(fromNode(n));
            }
        } else if (root.has("stations") && root.get("stations").isArray()) {
            for (JsonNode n : root.get("stations")) {
                result.add(fromNode(n));
            }
        } else if (root.isObject()) {
            for (Iterator<String> it = root.fieldNames(); it.hasNext(); ) {
                String key = it.next();
                JsonNode val = root.get(key);
                if (val.isArray()) {
                    for (JsonNode n : val) {
                        result.add(fromNode(n));
                    }
                }
            }
        }
        return result;
    }

    private StationProperties fromNode(JsonNode n) {
        StationProperties sp = new StationProperties();
        sp.setName(text(n, "name", "station", "Название", "станция"));
        sp.setLine(text(n, "line", "lineName", "Линия", "линия", "branch"));

        String dateRaw = text(n, "date", "opened", "Дата", "дата_открытия");
        String date = normalizeDate(dateRaw);
        if (date != null) {
            sp.setDate(date);
        }

        String depthRaw = text(n, "depth", "глубина");
        Double depth = parseDepth(depthRaw);
        if (depth != null) {
            sp.setDepth(depth);
        }

        Boolean conn = bool(n, "hasConnection", "пересадка", "has_transfer");
        if (conn != null) {
            sp.setHasConnection(conn);
        }

        return sp;
    }

    private static String text(JsonNode n, String... keys) {
        for (String k : keys) {
            JsonNode v = n.get(k);
            if (v != null && !v.isNull()) {
                return v.asText().trim();
            }
        }
        return null;
    }

    private static Boolean bool(JsonNode n, String... keys) {
        for (String k : keys) {
            JsonNode v = n.get(k);
            if (v != null && !v.isNull()) {
                if (v.isBoolean()) {
                    return v.asBoolean();
                }
                String s = v.asText().trim().toLowerCase();
                if (s.matches("^(true|yes|да|1)$")) {
                    return true;
                }
                if (s.matches("^(false|no|нет|0)$")) {
                    return false;
                }
            }
        }
        return null;
    }

    private static Double parseDepth(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String norm = s.replace(",", ".").replaceAll("[^\\-0-9.]", "");
        if (norm.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(norm);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String normalizeDate(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                LocalDate d = LocalDate.parse(s.trim(), fmt);
                return d.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            } catch (Exception ignored) {
                // try next format
            }
        }
        // Try extracting digits like 13.10.1962 from “13 октября 1962”
        String digits = s.replaceAll("[^0-9.]", " ").replaceAll("\\s+", " ").trim();
        if (digits.matches("\\d{1,2}\\.\\d{1,2}\\.\\d{4}")) {
            String[] parts = digits.split("\\.");
            String dd = String.format("%02d", Integer.parseInt(parts[0]));
            String mm = String.format("%02d", Integer.parseInt(parts[1]));
            return dd + "." + mm + "." + parts[2];
        }
        return null;
    }
}
