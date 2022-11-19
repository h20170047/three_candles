package com.svj.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection="DailyStockData")
public class StockDayData {
    @Id
    private String id;
    private String SYMBOL;
    private String SERIES;
    private double OPEN;
    private double HIGH;
    private double LOW;
    private double CLOSE;
    private double LAST;
    private double PREVCLOSE;
    private int TOTTRDQTY;
    private double TOTTRDVAL;
    private LocalDate TIMESTAMP;
}
