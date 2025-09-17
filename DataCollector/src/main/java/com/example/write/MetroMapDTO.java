package com.example.write;

import com.example.model.Line;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MetroMapDTO {
    private List<Line> lines;
    private Map<String, List<String>> stations;

    public MetroMapDTO() {}
    public MetroMapDTO(List<Line> lines, Map<String, List<String>> stations) {
        this.lines = lines; this.stations = stations;
    }
}