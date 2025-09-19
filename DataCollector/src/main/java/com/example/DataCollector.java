package com.example;

import com.example.write.DepthAggregator;
import com.example.model.Line;
import com.example.model.Station;
import com.example.model.StationProperties;
import com.example.parse.CsvStationParser;
import com.example.parse.FileFinder;
import com.example.parse.JsonStationsParser;
import com.example.parse.WebMetroParser;
import com.example.write.OutputWriter;
import com.example.write.OutputWriter.ConnectionOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataCollector {
    private static final Logger LOG = LoggerFactory.getLogger(DataCollector.class);

    private static final java.util.Set<String> IGNORE_JSON_BASENAMES =
            java.util.Set.of("map.json", "stations.json");

    private static class ProjectPaths {
        final Path dataRoot;
        final Path outDir;
        ProjectPaths(Path dataRoot, Path outDir) {
            this.dataRoot = dataRoot;
            this.outDir = outDir;
        }
    }

    public static void main(String[] args) throws Exception {
        ProjectPaths p = resolvePaths(args);

        WebMetroParser.ParsedMetro metro = parseWeb();
        LOG.info("Lines: {} | Stations: {} | Connection groups: {}",
                metro.lines.size(), metro.stations.size(), metro.connections.size());

        FileFinder.Result found = findFiles(p.dataRoot);
        LOG.info("Found JSON: {} | CSV: {}", found.json.size(), found.csv.size());

        Map<String, StationProperties> mergedProps = parseAndMergeProps(found);
        LOG.info("Merged StationProperties: {}", mergedProps.size());

        attachPropsToStations(metro.stations, mergedProps);

        enrichPropsWithLineAndConnections(
                mergedProps,
                metro.stations,
                metro.lines,
                metro.connections
        );

        // конвертируем пересадки в формат writer-а и пишем файлы
        List<List<ConnectionOut>> connOut = OutputWriter.convertConnections(metro.connections);
        writeOutputs(p.outDir, metro.lines, metro.stations, mergedProps.values(), connOut);

        LOG.info("Done. Files written to {}", p.outDir.toAbsolutePath());
    }

    private static ProjectPaths resolvePaths(String[] args) {
        Path outDir = Path.of("out");
        List<Path> candidates = new ArrayList<>();

        if (args.length > 0) {
            candidates.add(Path.of(args[0]));
        }

        candidates.add(Path.of("FilesAndNetwork", "DataCollector", "data"));
        candidates.add(Path.of("src", "main", "java", "com", "example", "data"));
        candidates.add(Path.of("src", "main", "resources", "data"));

        for (Path p : candidates) {
            if (java.nio.file.Files.isDirectory(p)) {
                System.out.println("Использую папку данных: " + p.toAbsolutePath());
                return new ProjectPaths(p, outDir);
            }
        }
        throw new IllegalArgumentException(
                "Папка с данными не найдена. Передай путь аргументом или помести данные в одну из стандартных папок.");
    }

    private static WebMetroParser.ParsedMetro parseWeb() throws Exception {
        WebMetroParser web = new WebMetroParser();
        String html = web.fetchHtml();
        return web.parse(html);
    }

    private static FileFinder.Result findFiles(Path dataRoot) throws Exception {
        FileFinder ff = new FileFinder();
        return ff.find(dataRoot);
    }

    private static Map<String, StationProperties> parseAndMergeProps(FileFinder.Result found) throws Exception {
        JsonStationsParser jsonParser = new JsonStationsParser();
        CsvStationParser csvParser = new CsvStationParser();
        DepthAggregator depthAgg = new DepthAggregator();

        List<StationProperties> propsFromJson = new ArrayList<>();
        for (Path jf : found.json) {
            String base = jf.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
            if (IGNORE_JSON_BASENAMES.contains(base)) {
                LOG.debug("Skipping sample format JSON: {}", jf);
                continue;
            }
            try {
                propsFromJson.addAll(jsonParser.parse(jf));
            } catch (Exception e) {
                LOG.debug("Skipping non-station JSON: {}", jf, e);
            }
        }

        List<StationProperties> propsFromCsv = new ArrayList<>();
        for (Path cf : found.csv) {
            try {
                propsFromCsv.addAll(csvParser.parse(cf));
            } catch (Exception e) {
                LOG.warn("CSV parse failed: {}", cf, e);
            }
        }

        Map<String, StationProperties> merged = new LinkedHashMap<>();
        List<StationProperties> allProps = new ArrayList<>();
        allProps.addAll(propsFromJson);
        allProps.addAll(propsFromCsv);

        for (StationProperties sp : allProps) {
            if (sp.getName() == null) {
                continue;
            }
            String linePart = sp.getLine() != null ? sp.getLine() : "?";
            String key = (linePart + "|" + sp.getName()).toLowerCase();
            merged.merge(key, sp, DataCollector::mergeProps);
        }

        Map<String, Double> depthByName = depthAgg.readDepths(found.json);
        for (StationProperties sp : merged.values()) {
            Double ext = depthByName.get(sp.getName());
            if (ext == null) {
                continue;
            }
            if (sp.getDepth() == null || Math.abs(ext) > Math.abs(sp.getDepth())) {
                sp.setDepth(ext);
            }
        }
        return merged;
    }

    private static void attachPropsToStations(List<Station> stations, Map<String, StationProperties> merged) {
        Map<String, StationProperties> byName = merged.values().stream()
                .collect(Collectors.toMap(
                        StationProperties::getName,
                        x -> x,
                        DataCollector::mergeProps
                ));
        for (Station st : stations) {
            StationProperties sp = byName.get(st.getName());
            if (sp != null) {
                st.setStationProperties(sp);
            }
        }
    }

    private static void writeOutputs(Path outDir,
                                     List<Line> lines,
                                     List<Station> stations,
                                     Collection<StationProperties> props,
                                     List<List<ConnectionOut>> connections) throws Exception {
        OutputWriter writer = new OutputWriter();
        writer.writeMapJson(outDir, lines, stations, connections);
        writer.writeStationsJson(outDir, new ArrayList<>(props));
    }

    private static StationProperties mergeProps(StationProperties a, StationProperties b) {
        StationProperties r = new StationProperties();
        r.setName(a.getName() != null ? a.getName() : b.getName());
        r.setLine(a.getLine() != null ? a.getLine() : b.getLine());
        r.setDate(a.getDate() != null ? a.getDate() : b.getDate());

        Double da = a.getDepth();
        Double db = b.getDepth();
        if (da == null) {
            r.setDepth(db);
        } else if (db == null) {
            r.setDepth(da);
        } else {
            r.setDepth(Math.abs(db) > Math.abs(da) ? db : da);
        }

        Boolean ca = a.getHasConnection();
        Boolean cb = b.getHasConnection();
        if (Boolean.TRUE.equals(ca) || Boolean.TRUE.equals(cb)) {
            r.setHasConnection(true);
        } else if (ca == null && cb == null) {
            r.setHasConnection(null);
        } else {
            r.setHasConnection(false);
        }
        return r;
    }

    private static void enrichPropsWithLineAndConnections(
            Map<String, StationProperties> merged,
            List<Station> stationsFromWeb,
            List<Line> linesFromWeb,
            List<List<com.example.parse.WebMetroParser.ConnectionNode>> connections
    ) {

        Map<String, String> lineNumToName = new LinkedHashMap<>();
        for (Line ln : linesFromWeb) {
            lineNumToName.put(ln.getNumber(), ln.getName());
        }

        Map<String, String> stationNameToLineNum = new LinkedHashMap<>();
        for (Station st : stationsFromWeb) {
            stationNameToLineNum.putIfAbsent(st.getName(), st.getLineNumber());
        }

        java.util.Set<String> hasConnByName = new java.util.HashSet<>();
        if (connections != null) {
            for (var group : connections) {
                if (group == null || group.size() < 2) {
                    continue;
                }
                for (var c : group) {
                    hasConnByName.add(c.station);
                }
            }
        }


        for (StationProperties sp : merged.values()) {
            // 1) line (имя линии): если пусто — берём по номеру, полученному с веб-страницы
            if (sp.getLine() == null || sp.getLine().isBlank()) {
                String lineNum = stationNameToLineNum.get(sp.getName());
                if (lineNum != null) {
                    String lineName = lineNumToName.get(lineNum);
                    if (lineName != null && !lineName.isBlank()) {
                        sp.setLine(lineName);
                    }
                }
            }

            boolean computedHasConn = hasConnByName.contains(sp.getName());
            if (sp.getHasConnection() == null) {
                sp.setHasConnection(computedHasConn);
            } else if (!sp.getHasConnection() && computedHasConn) {
                sp.setHasConnection(true);
            }
        }
    }
}
