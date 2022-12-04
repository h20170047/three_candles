package com.svj.utils;

import java.time.format.DateTimeFormatter;

public class AppUtils {
    public static DateTimeFormatter dateTimeFormatter= DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");

    public static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d-M-yyyy");
}
