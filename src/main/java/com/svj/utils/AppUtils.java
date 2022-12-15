package com.svj.utils;

import com.svj.exception.FileException;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class AppUtils {
    public static DateTimeFormatter dateTimeFormatter= DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    public static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d-M-yyyy");


    public static List<String> getResourceFileAsStringList(String fileName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null){
                throw new FileException(String.format("Missing %s file in classpath", fileName));
            }
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                List<String> lines = reader.lines().collect(Collectors.toList());
                return lines;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String getFileNameFromDate(LocalDate day) {
        String dayOfMonth= day.getDayOfMonth()<10?"0".concat(String.valueOf(day.getDayOfMonth())): String.valueOf(day.getDayOfMonth());
        String monthAlpha= day.getMonth().toString().substring(0, 3);
        String yearStr= String.valueOf(day.getYear());
        return "cm".concat(dayOfMonth).concat(monthAlpha).concat(yearStr).concat("bhav.csv");
    }
}
