package com.example;

import com.example.model.Line;
import com.example.model.Station;
import com.example.model.StationProperties;
import com.example.parse.FileFinder;
import com.example.parse.JsonStationsParser;
import com.example.parse.CsvStationParser;
import com.example.parse.WebMetroParser;
import com.example.write.OutputWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        // 1) Parse web page
        WebMetroParser web = new WebMetroParser();
        String html = web.fetchHtml();
        WebMetroParser.ParsedMetro metro = web.parse(html);
        List<Line> lines = metro.lines;
        List<Station> stations = metro.stations;

        LOGGER.info("Lines parsed: {}", lines.size());
        LOGGER.info("Stations parsed: {}", stations.size());

        // 2) Find JSON/CSV files in the unzipped archive directory
        // Change this to your path with data from the assignment archive:
        Path dataRoot = Path.of("data"); // e.g. "/Users/macbook/Downloads/archive-root"
        FileFinder finder = new FileFinder();
        FileFinder.FoundFiles found = finder.findJsonAndCsv(dataRoot);

        LOGGER.info("JSON files: {}", found.jsonFiles);
        LOGGER.info("CSV files:  {}", found.csvFiles);

        // 3) Parse JSON files into StationProperties
        JsonStationsParser jsonParser = new JsonStationsParser();
        List<StationProperties> enriched = new ArrayList<>();
        for (Path jf : found.jsonFiles) {
            try {
                enriched.addAll(jsonParser.parse(jf));
            } catch (Exception e) {
                LOGGER.warn("Failed to parse JSON {}: {}", jf, e.getMessage());
            }
        }

        // 4) Parse CSV files into StationProperties
        CsvStationParser csvParser = new CsvStationParser();
        for (Path cf : found.csvFiles) {
            try {
                enriched.addAll(csvParser.parse(cf));
            } catch (Exception e) {
                LOGGER.warn("Failed to parse CSV {}: {}", cf, e.getMessage());
            }
        }

        // 5) Write outputs
        OutputWriter writer = new OutputWriter();
        Path out = Path.of("output"); // project-relative; appears in FilesAndNetwork/DataCollector/output
        writer.writeMapJson(out, lines, stations);
        writer.writeStationsJson(out, mergedUnique(enriched));

        LOGGER.info("Done. Files written to: {}", out.toAbsolutePath());
    }

    // Deduplicate by (name,line) keeping first non-null props
    private static List<StationProperties> mergedUnique(List<StationProperties> list) {
        return list.stream()
                .collect(java.util.stream.Collectors.toMap(
                        sp -> ((sp.getName()!=null?sp.getName():"") + "||" + (sp.getLine()!=null?sp.getLine():""))
                                .toLowerCase(),
                        sp -> sp,
                        (a,b) -> merge(a,b),
                        java.util.LinkedHashMap::new
                ))
                .values().stream().toList();
    }

    private static StationProperties merge(StationProperties a, StationProperties b) {
        if (a.getName() == null) a.setName(b.getName());
        if (a.getLine() == null) a.setLine(b.getLine());
        if (a.getDate() == null) a.setDate(b.getDate());
        if (a.getDepth() == null) a.setDepth(b.getDepth());
        if (a.getHasConnection() == null) a.setHasConnection(b.getHasConnection());
        return a;
    }
}
