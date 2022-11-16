package com.svj.service;

import com.google.gson.Gson;
import com.svj.entity.Stock;
import com.svj.utils.AppUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.apache.commons.io.IOUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class NSEServiceTest {

    private NSEService NSEService;

    @BeforeEach
    public void setup(){
        NSEService= new NSEService();
    }

    @Test
    public void getListOfBullBearUnknowns(){
        Map<String, List<String>> result= NSEService.getStocksListForDay();
        System.out.println(String.format("Total bullish: %s, bearish: %s", result.get("bullish").size(), result.get("bearish").size()));
        System.out.println("Bullish "+ result.get("bullish").toString());
        System.out.println("Bearish "+ result.get("bearish").toString());
        assertThat(result.getOrDefault("bullish", null)).isNotEmpty();
        assertThat(result.getOrDefault("bearish", null)).isNotEmpty();
    }

    @Test
    public void getOldDataTest(){
        NSEService.getOldData();
    }

    DateTimeFormatter df = DateTimeFormatter.ofPattern("d-M-yyyy");
    @Test
    public void getBusinessDays(){
        LocalDate startDate= LocalDate.parse("08-11-2022", df);
        LocalDate endDate= LocalDate.parse("15-11-2022", df);
        List<LocalDate> holidays= Arrays.asList(LocalDate.parse("8-11-2022", df));
        List<LocalDate> businessDays= NSEService.getBusinessDays(startDate, endDate, holidays);
        assertThat(businessDays.size()).isEqualTo(4);
    }

    @Test
    public void getNSEData(){
        LocalDate startDate= LocalDate.parse("1-1-1997", df);
        LocalDate endDate= LocalDate.parse("15-11-2022", df);
        List<LocalDate> holidays= NSEService.getHolidays();
        List<LocalDate> businessDays = NSEService.getBusinessDays(startDate, endDate, holidays);
//        for(LocalDate day: businessDays){
//            NSEService.getBhavCopy(day);
//        }
    }
}