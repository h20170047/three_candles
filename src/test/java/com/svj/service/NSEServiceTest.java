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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class NSEServiceTest {

    private NSEService NSEService;

    @BeforeEach
    public void setup(){
        NSEService= new NSEService();
    }


    @Test
    public void getDataFromNSE() throws IOException {
        String data= NSEService.getDataForDay();
//        System.out.println(data);
        assertThat(data).isNotEmpty();
    }

    @Test
    public void testIfDataIsCaptured(){
        try {
            File f = new File("src/test/resources/sampleData.json");
            if (f.exists()) {
                InputStream is = new FileInputStream("src/test/resources/sampleData.json");
                String jsonTxt = IOUtils.toString(is, "UTF-8");
                List<Stock> dailyData= NSEService.convertTextToStocks(jsonTxt);
                for(Stock stock: dailyData){
                    System.out.print("\""+stock.getSymbol()+"\""+", ");
                }
                assertThat(dailyData.size()).isEqualTo(51); //NiftyIndex + 50 stocks
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void checkDataCollectionWithParse(){
        List<Stock> stockList= NSEService.processData();
        System.out.println(stockList);
        assertThat(stockList.size()).isEqualTo(51);
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
}
