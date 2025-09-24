package com.example.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StationProperties {

    private String name;            // "Название станции"
    private String line;            // "Название линии"
    private String date;            // "ДД.ММ.ГГГГ"
    private Double depth;           // число (может быть null)
    private Boolean hasConnection;  // true/false

}
