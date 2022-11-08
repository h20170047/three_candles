package com.svj.utils;

import com.svj.entity.Stock;
import com.svj.exception.NSEDataProcessingException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.format.datetime.joda.LocalDateTimeParser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AppUtils {
    public static DateTimeFormatter dateTimeFormatter= DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
    public static Stock convertToStockEntity(JSONObject o) {
        try{
            Stock result= new Stock(
                    o.get("symbol").toString(),
                    Double.parseDouble(o.get("lastPrice").toString()),
                    Double.parseDouble(o.get("dayHigh").toString()),
                    Double.parseDouble(o.get("dayLow").toString()),
                    Double.parseDouble(o.get("open").toString()),
                    LocalDateTime.parse(o.get("lastUpdateTime").toString(), dateTimeFormatter));
            return result;
        } catch (JSONException e) {
            throw new NSEDataProcessingException(e.getMessage());
        }
    }
}
