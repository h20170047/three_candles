package com.svj.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AppUtils {
    public static DateTimeFormatter dateTimeFormatter= DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    public static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d-M-yyyy");


    public static List<String> getResourceFileAsStringList(String fileName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
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
}
