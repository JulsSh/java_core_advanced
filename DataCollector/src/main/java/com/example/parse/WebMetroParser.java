package com.example.parse;

import com.example.model.Line;
import com.example.model.Station;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebMetroParser {
    private static final Logger LOG = LoggerFactory.getLogger(WebMetroParser.class);
    private static final String URL = "https://skillbox-java.github.io/";
    private static final Pattern LN = Pattern.compile("\\bln-(\\d+)\\b");

    /** Загрузка HTML. */
    public String fetchHtml() throws IOException {
        Connection conn = Jsoup.connect(URL)
                .userAgent("Mozilla/5.0")
                .timeout((int) Duration.ofSeconds(15).toMillis());
        return conn.get().outerHtml();
    }

    /** Узел пересадки (линия+станция). */
    public static class ConnectionNode {
        public final String lineNumber;
        public final String station;
        public ConnectionNode(String lineNumber, String station) {
            this.lineNumber = lineNumber;
            this.station = station;
        }
    }

    /** Результат парсинга. */
    public static class ParsedMetro {
        public final List<Line> lines;
        public final List<Station> stations;
        public final List<List<ConnectionNode>> connections;
        public ParsedMetro(List<Line> lines, List<Station> stations, List<List<ConnectionNode>> connections) {
            this.lines = lines;
            this.stations = stations;
            this.connections = connections;
        }
    }

    /** Короткий метод parse — вызывает хелперы. */
    public ParsedMetro parse(String html) {
        Document doc = Jsoup.parse(html);
        Element root = findRoot(doc);

        Map<String, Line> linesByNumber = new LinkedHashMap<>();
        List<Station> stations = new ArrayList<>();

        Map<String, Integer> nodeIdByKey = new LinkedHashMap<>();
        List<ConnectionNode> nodes = new ArrayList<>();
        UnionFind uf = new UnionFind();

        parseLinesStationsAndConnections(root, linesByNumber, stations, nodeIdByKey, nodes, uf);

        List<List<ConnectionNode>> connections = groupConnections(nodes, uf);

        LOG.info("Skillbox: линии={} станции={} пересадки={}",
                linesByNumber.size(), stations.size(), connections.size());

        return new ParsedMetro(new ArrayList<>(linesByNumber.values()), stations, connections);
    }

    /* ===================== ХЕЛПЕРЫ ===================== */

    private Element findRoot(Document doc) {
        Element root = doc.selectFirst("#metrodata");
        if (root == null) {
            LOG.warn("#metrodata не найден — используем весь документ");
            return doc;
        }
        return root;
    }

    private void parseLinesStationsAndConnections(Element root,
                                                  Map<String, Line> linesByNumber,
                                                  List<Station> stations,
                                                  Map<String, Integer> nodeIdByKey,
                                                  List<ConnectionNode> nodes,
                                                  UnionFind uf) {

        for (Element lineEl : root.select(".js-metro-line")) {
            parseLine(root, lineEl, linesByNumber, stations, nodeIdByKey, nodes, uf);
        }
    }

    private void parseLine(Element root,
                           Element lineEl,
                           Map<String, Line> linesByNumber,
                           List<Station> stations,
                           Map<String, Integer> nodeIdByKey,
                           List<ConnectionNode> nodes,
                           UnionFind uf) {

        String number = lineEl.attr("data-line").trim();
        String name = lineEl.text().trim();
        if (number.isEmpty() || name.isEmpty()) {
            return;
        }
        linesByNumber.putIfAbsent(number, new Line(number, name));

        Elements stationEls = selectStationElements(root, number);
        for (Element nameEl : stationEls) {
            processStation(root, number, nameEl, stations, nodeIdByKey, nodes, uf);
        }
    }

    private Elements selectStationElements(Element root, String number) {
        Elements els = root.select(".js-metro-stations[data-line='" + number + "'] .name");
        if (els.isEmpty()) {
            els = root.select("[data-line='" + number + "'] .name, "
                    + ".js-metro-stations[data-line='" + number + "'] a.name, "
                    + ".js-metro-stations[data-line='" + number + "'] span.name");
        }
        return els;
    }

    private void processStation(Element root,
                                String lineNumber,
                                Element nameEl,
                                List<Station> stations,
                                Map<String, Integer> nodeIdByKey,
                                List<ConnectionNode> nodes,
                                UnionFind uf) {

        String stationName = nameEl.text().trim();
        if (stationName.isEmpty()) {
            return;
        }
        stations.add(new Station(stationName, lineNumber));

        int aId = ensureNode(nodeIdByKey, nodes, uf, lineNumber, stationName);

        Element row = findStationRow(nameEl);
        for (Element icon : row.select(".t-icon-metroln")) {
            String targetLine = extractLineFromClass(icon.className());
            if (targetLine == null || targetLine.equals(lineNumber)) {
                continue;
            }
            String targetName = extractStationFromTitle(icon.attr("title"));
            if (targetName == null || targetName.isBlank()) {
                continue;
            }
            int bId = ensureNode(nodeIdByKey, nodes, uf, targetLine, targetName);
            uf.union(aId, bId);
        }
    }

    private int ensureNode(Map<String, Integer> nodeIdByKey,
                           List<ConnectionNode> nodes,
                           UnionFind uf,
                           String lineNumber,
                           String stationName) {
        String key = lineNumber + "|" + stationName;
        Integer id = nodeIdByKey.get(key);
        if (id != null) {
            return id;
        }
        int nid = nodes.size();
        nodes.add(new ConnectionNode(lineNumber, stationName));
        uf.add(nid);
        nodeIdByKey.put(key, nid);
        return nid;
    }

    private Element findStationRow(Element nameEl) {
        Element cur = nameEl;
        Set<String> tags = Set.of("p", "li", "div");
        while (cur != null) {
            if (tags.contains(cur.tagName().toLowerCase(Locale.ROOT))) {
                return cur;
            }
            cur = cur.parent();
        }
        return nameEl.parent();
    }

    private String extractLineFromClass(String cls) {
        Matcher m = LN.matcher(cls);
        return m.find() ? m.group(1) : null;
    }

    private String extractStationFromTitle(String title) {
        if (title == null) {
            return null;
        }
        String t = title.trim();
        if (t.isEmpty()) {
            return null;
        }
        Matcher q = Pattern.compile("[«\"]([^»\"]+)[»\"]").matcher(t);
        if (q.find()) {
            return q.group(1).trim();
        }
        Matcher w = Pattern.compile("станц[ияи]\\s+([A-Za-zА-Яа-яЁё\\-\\s]+)$").matcher(t);
        if (w.find()) {
            return w.group(1).trim();
        }
        return t;
    }

    private List<List<ConnectionNode>> groupConnections(List<ConnectionNode> nodes, UnionFind uf) {
        Map<Integer, List<ConnectionNode>> groups = new LinkedHashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            int root = uf.find(i);
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(nodes.get(i));
        }
        List<List<ConnectionNode>> result = new ArrayList<>();
        for (List<ConnectionNode> g : groups.values()) {
            List<ConnectionNode> uniq = dedup(g);
            if (uniq.size() >= 2) {
                result.add(uniq);
            }
        }
        return result;
    }

    private List<ConnectionNode> dedup(List<ConnectionNode> list) {
        LinkedHashMap<String, ConnectionNode> map = new LinkedHashMap<>();
        for (ConnectionNode c : list) {
            map.put(c.lineNumber + "|" + c.station, c);
        }
        return new ArrayList<>(map.values());
    }

    /** Простейший Union-Find. */
    private static class UnionFind {
        private final List<Integer> p = new ArrayList<>();
        private final List<Integer> r = new ArrayList<>();
        void add(int i) {
            while (p.size() <= i) {
                p.add(p.size());
                r.add(0);
            }
        }
        int find(int x) {
            if (!p.get(x).equals(x)) {
                p.set(x, find(p.get(x)));
            }
            return p.get(x);
        }
        void union(int a, int b) {
            int ra = find(a), rb = find(b);
            if (ra == rb) {
                return;
            }
            int raR = r.get(ra), rbR = r.get(rb);
            if (raR < rbR) {
                p.set(ra, rb);
            } else if (raR > rbR) {
                p.set(rb, ra);
            } else {
                p.set(rb, ra);
                r.set(ra, raR + 1);
            }
        }
    }
}
