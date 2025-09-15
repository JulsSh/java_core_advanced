package com.example.write;

import com.example.model.Line;
import com.example.model.Station;
import com.example.model.StationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;          // ✅
import com.fasterxml.jackson.databind.SerializationFeature;  // ✅


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OutputWriter {

private  final ObjectMapper mp = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    public void writeMapJson(Path targetDir, List<Line> lines, List<Station> stations) throws IOException {
        Map<String, List<String>> stationsByLine = stations.stream()
                .collect(Collectors.groupingBy(
                        Station::getLineNumber,
                        LinkedHashMap::new,
                        Collectors.mapping(Station::getName, Collectors.toList())
                ));
MetroMapDTO dto = new MetroMapDTO();
       // MetroMapDto dto = new MetroMapDto(lines, stationsByLine);
        write(targetDir.resolve("map.json"), dto);
    }

    public void writeStationsJson(Path targetDir, List<StationProperties> stations) throws IOException {
        // Wrap into {"stations":[ ... ]}
        Map<String, Object> wrapper = Map.of("stations", stations);
        write(targetDir.resolve("stations.json"), wrapper);
    }

    private void write(Path file, Object data) throws IOException {
        Files.createDirectories(file.getParent());
        mp.writeValue(file.toFile(), data);
    }
}