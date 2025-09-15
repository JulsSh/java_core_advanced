package com.example.parse;

import com.example.model.Line;
import com.example.model.Station;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WebMetroParser {
    private static final Logger LOG = LoggerFactory.getLogger(WebMetroParser.class);

    // Wikipedia page with the list of stations (structure can change; adjust selectors if needed)
    private static final String URL = "https://ru.wikipedia.org/wiki/Список_станций_Московского_метрополитена";

    public String fetchHtml() throws IOException {
        LOG.info("Fetching HTML from {}", URL);
        Document doc = Jsoup.connect(URL).timeout((int) Duration.ofSeconds(15).toMillis()).get();
        return doc.outerHtml();
    }

    public static class ParsedMetro {
        public final List<Line> lines;
        public final List<Station> stations;

        public ParsedMetro(List<Line> lines, List<Station> stations) {
            this.lines = lines;
            this.stations = stations;
        }
    }

    /** Parse HTML and extract lines and stations. */
    public ParsedMetro parse(String html) {
        Document doc = Jsoup.parse(html);

        Elements tables = doc.select("table.wikitable");

        Map<String, Line> linesByNumber = new LinkedHashMap<>();
        List<Station> stations = new ArrayList<>();

        for (Element table : tables) {
            for (Element row : table.select("tr")) {
                Elements cells = row.select("td");
                if (cells.size() < 3) {
                    continue;
                }

                String stationName = text(cells.get(1));
                String lineText = text(cells.get(0));

                if (stationName.isBlank() || lineText.isBlank()) {
                    continue;
                }

                // Extract line number (e.g. "1") and line name from mixed text like "1 — Сокольническая"
                String[] split = lineText.split("—|-", 2);
                String lineNumber = split[0].trim().replaceAll("[^0-9А-Яа-яA-Za-z]+", "");
                if (lineNumber.isBlank()) {
                    lineNumber = lineText.trim();
                }
                String lineName = (split.length > 1) ? split[1].trim() : lineText.trim();

                linesByNumber.putIfAbsent(lineNumber, new Line(lineNumber, lineName));
                stations.add(new Station(stationName, lineNumber));
            }
        }

        LOG.info("Parsed {} lines, {} stations (heuristic)", linesByNumber.size(), stations.size());
        return new ParsedMetro(new ArrayList<>(linesByNumber.values()), stations);
    }

    private static String text(Element e) {
        return e.text().trim();
    }
}
