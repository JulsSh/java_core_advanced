package com.example.write;

import com.example.model.Line;
import com.example.model.Station;
import com.example.model.StationProperties;
import com.example.parse.WebMetroParser;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class OutputWriter {
    private final ObjectMapper mp = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    /** SPBMetro: lines[].number должен быть числом */
    public static class LineOut {
        public int number;
        public String name;
        public LineOut(int number, String name) { this.number = number; this.name = name; }
    }

    /** Элемент connections: { "line": <int>, "station": "<name>" } */
    public static class ConnectionOut {
        public int line;
        public String station;
        public ConnectionOut(int line, String station) { this.line = line; this.station = station; }
    }

    public void writeMapJson(Path targetDir,
                             List<Line> lines,
                             List<Station> stations,
                             List<List<ConnectionOut>> connections) throws IOException {

        Map<String, List<String>> stationsByLine = stations.stream()
                .collect(Collectors.groupingBy(
                        Station::getLineNumber,
                        LinkedHashMap::new,
                        Collectors.mapping(Station::getName, Collectors.toList())
                ));

        List<LineOut> lineOut = lines.stream()
                .map(l -> new LineOut(parseIntSafe(l.getNumber()), l.getName()))
                .collect(Collectors.toList());

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("stations", stationsByLine);
        root.put("lines", lineOut);
        if (connections != null && !connections.isEmpty()) {
            root.put("connections", connections);
        }

        write(targetDir.resolve("map.json"), root);
    }

    public void writeStationsJson(Path targetDir, Collection<StationProperties> stationsProps) throws IOException {
        Map<String, Object> wrapper = Map.of("stations", stationsProps);
        write(targetDir.resolve("stations.json"), wrapper);
    }

    private void write(Path file, Object data) throws IOException {
        Files.createDirectories(file.getParent());
        mp.writeValue(file.toFile(), data);
    }

    private static int parseIntSafe(String s) {
        try { return Integer.parseInt(s.replaceAll("\\D+", "")); }
        catch (Exception e) { return 0; }
    }

    /** Преобразование из парсера в формат записи. */
    public static List<List<ConnectionOut>> convertConnections(List<List<WebMetroParser.ConnectionNode>> in) {
        if (in == null) { return List.of(); }
        List<List<ConnectionOut>> out = new ArrayList<>();
        for (List<WebMetroParser.ConnectionNode> group : in) {
            List<ConnectionOut> g = new ArrayList<>();
            for (WebMetroParser.ConnectionNode c : group) {
                g.add(new ConnectionOut(parseIntSafe(c.lineNumber), c.station));
            }
            // Удалим возможные дубликаты внутри группы
            LinkedHashMap<String, ConnectionOut> uniq = new LinkedHashMap<>();
            for (ConnectionOut co : g) {
                uniq.put(co.line + "|" + co.station, co);
            }
            if (uniq.size() >= 2) {
                out.add(new ArrayList<>(uniq.values()));
            }
        }
        return out;
    }
}
