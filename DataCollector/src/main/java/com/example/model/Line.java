package com.example.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Line {
    private String number; // e.g. "1"
    private String name;

    public Line(String number, String name) {
        this.number = number;
        this.name = name;
    }
}
