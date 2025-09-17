package com.example;

import com.example.write.DepthAggregator;                  // у тебя DepthAggregator лежит в write
import com.example.model.Line;
import com.example.model.Station;
import com.example.model.StationProperties;
import com.example.parse.CsvStationParser;
import com.example.parse.FileFinder;
import com.example.parse.JsonStationsParser;
import com.example.parse.WebMetroParser;
import com.example.write.OutputWriter;
import com.example.write.OutputWriter.ConnectionOut;        // <- для connections
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

    // Небольшой DTO для путей (класс, не record — дружит с Checkstyle)
    private static class ProjectPaths {
        final Path dataRoot;
        final Path outDir;

        ProjectPaths(Path dataRoot, Path outDir) {
            this.dataRoot = dataRoot;
            this.outDir = outDir;
        }
    }

    // --- короткий main (≤ 50 непустых строк) ---
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

        // конвертируем пересадки в формат writer-а
        List<List<ConnectionOut>> connOut = OutputWriter.convertConnections(metro.connections);

        writeOutputs(p.outDir, metro.lines, metro.stations, mergedProps.values(), connOut);

        LOG.info("Done. Files written to {}", p.outDir.toAbsolutePath());
    }
    // --- конец короткого main ---

    private static ProjectPaths resolvePaths(String[] args) {
        Path outDir = Path.of("out");
        List<Path> candidates = new ArrayList<>();

        if (args.length > 0) {
            candidates.add(Path.of(args[0]));
        }
        // стандартные варианты расположения данных
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
            try {
                propsFromJson.addAll(jsonParser.parse(jf));
            } catch (Exception e) {
                // вероятно depth-файл или иной формат — пропускаем
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

        // подтягиваем глубины из специальных JSON-ов (station_name/depth) с приоритетом большей глубины
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
        writer.writeMapJson(outDir, lines, stations, connections); // <- теперь с connections
        writer.writeStationsJson(outDir, new ArrayList<>(props));
    }

    // слияние StationProperties (глубина — по большей абсолютной, hasConnection — логическое ИЛИ, остальные — первый ненулевой)
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
}
