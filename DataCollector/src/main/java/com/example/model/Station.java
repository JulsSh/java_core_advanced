package com.example.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Station {
        private String name;
        private String lineNumber; // link to Line.number

    public Station(String name, String lineNumber) {
        this.name = name;
        this.lineNumber = lineNumber;
    }
}
