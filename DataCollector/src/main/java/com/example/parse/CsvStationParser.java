package com.example.parse;

import com.example.model.StationProperties;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvStationParser {

    // Assumes header row with columns like: name,line,date,depth,hasConnection (flexible order/names)
    public List<StationProperties> parse(Path csvFile) throws Exception {
        try (CSVReader reader = new CSVReaderBuilder(
                new FileReader(csvFile.toFile(), StandardCharsets.UTF_8)).withSkipLines(0).build()) {

            String[] header = reader.readNext();
            if (header == null) {
                return List.of();
            }
            Map<String, Integer> idx = index(header);

            List<StationProperties> result = new ArrayList<>();
            String[] row;
            while ((row = reader.readNext()) != null) {
                StationProperties sp = new StationProperties();
                sp.setName(get(row, idx, "name", "station", "Название", "станция"));
                sp.setLine(get(row, idx, "line", "Линия", "lineName"));
                sp.setDate(normalizeDate(get(row, idx, "date", "opened", "Дата")));
                sp.setDepth(parseDepth(get(row, idx, "depth", "глубина")));
                sp.setHasConnection(parseBool(get(row, idx, "hasConnection", "пересадка")));
                result.add(sp);
            }
            return result;
        }
    }

    private static Map<String, Integer> index(String[] header) {
        Map<String, Integer> m = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            m.put(header[i].trim().toLowerCase(), i);
        }
        return m;
    }

    private static String get(String[] row, Map<String, Integer> idx, String... keys) {
        for (String k : keys) {
            Integer i = idx.get(k.toLowerCase());
            if (i != null && i < row.length) {
                String v = row[i].trim();
                if (!v.isEmpty()) {
                    return v;
                }
            }
        }
        return null;
    }

    private static Boolean parseBool(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim().toLowerCase();
        if (t.matches("^(true|yes|да|1)$")) {
            return true;
        }
        if (t.matches("^(false|no|нет|0)$")) {
            return false;
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
        } catch (Exception e) {
            return null;
        }
    }

    private static String normalizeDate(String s) {
        // Reuse JsonStationsParser’s normalization
        return JsonStationsParser.normalizeDate(s);
    }
}
