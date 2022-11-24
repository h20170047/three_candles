package com.svj.service;

import com.svj.entity.Stock;
import org.apache.ant.compress.taskdefs.Unzip;
import org.apache.commons.io.IOUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;

public class NSEService {

    private String NSEDailyDataURL="https://www.nseindia.com/api/equity-stockIndices?index=NIFTY 50";

    private RestTemplate restTemplate= new RestTemplate();


    DateTimeFormatter dateTimeFormatter=
            new DateTimeFormatterBuilder()
                    .parseCaseInsensitive()
                    .appendPattern("dd-MMM-yyyy[ [HH][:mm][:ss][.SSS]]")
                    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                    .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                    .toFormatter();

    public Map<String, List<String>> getStocksListForDay() {
        // create map and list for 3 categories- bullish, bearish, unknown
        // get 2weeks data from the nse1 api, get the last 3 days info from today, if it is market-hours. else consider today too
        // return response map data
        Map<String, List<String>> result= new HashMap<>();
        List<String> nifty50Stocks= Arrays.asList("ADANIENT", "BAJAJFINSV", "HINDALCO", "ADANIPORTS", "JSWSTEEL", "ULTRACEMCO", "TATASTEEL", "TATAMOTORS", "RELIANCE", "UPL", "SBIN", "ONGC", "BRITANNIA", "ASIANPAINT", "BAJFINANCE", "GRASIM", "AXISBANK", "LT", "TITAN", "TATACONSUM", "INDUSINDBK", "WIPRO", "COALINDIA", "ITC", "TCS", "KOTAKBANK", "M&M", "MARUTI", "NESTLEIND", "BAJAJ-AUTO", "TECHM", "HCLTECH", "HDFC", "BHARTIARTL", "EICHERMOT", "SUNPHARMA", "NTPC", "ICICIBANK", "HDFCBANK", "APOLLOHOSP", "SBILIFE", "DIVISLAB", "POWERGRID", "HINDUNILVR", "CIPLA", "HDFCLIFE", "BPCL", "DRREDDY", "INFY", "HEROMOTOCO");
        String path= "C:\\Users\\svjra\\Documents\\git\\Springboot\\three_candles\\src\\main\\resources\\data";
        Map<String, List<Stock>> threeDaysInfo= new HashMap<>();
        List<String> bullish= new LinkedList<>();
        List<String> bearish= new LinkedList<>();
        try (Stream<Path> stream = Files.walk(Paths.get(path))) {
            stream.filter(Files::isRegularFile)
                    .forEach(file->{
                        System.out.println("Processing "+file.getFileName());
                        try {
                            Files.lines(file)
                                    .skip(1)
                                    .filter(line-> line.split(",")[1].equals("EQ"))
                                    .filter(line-> nifty50Stocks.contains(line.split(",")[0])) // if stock if nifty50, save to list
                                    .map(line-> line.split(","))
                                    .map(s-> new Stock(s[0], Double.parseDouble(s[5]), Double.parseDouble(s[3]), Double.parseDouble(s[4]), Double.parseDouble(s[2]), LocalDateTime.parse(s[10], dateTimeFormatter)))
                                    .forEach(stock-> {
                                        List<Stock> currList= threeDaysInfo.getOrDefault(stock.getSymbol(), new LinkedList<>());
                                        currList.add(stock);
                                        threeDaysInfo. put(stock.getSymbol(), currList);
                                    });

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(String stock: threeDaysInfo.keySet()){
            List<Stock> data= threeDaysInfo.get(stock);
            boolean isBullish= data.get(0).getOpen()< data.get(0).getClose();
            int i= 1;
            for(; i<3; i++){
                if(data.get(i).getOpen()< data.get(i).getClose()!= isBullish){
                    i= 10;
                }
            }
            if(i==3){
                boolean b = isBullish ? bullish.add(stock) : bearish.add(stock);
            }
        }
        result.put("bullish", bullish);
        result.put("bearish", bearish);
        return result;
    }



    public void getOldData(){
        getHolidays();
    }

    public List<LocalDate> getHolidays() {
        Scanner s = null;
        try {
            s = new Scanner(new File("C:\\Users\\svjra\\Documents\\git\\Springboot\\three_candles\\src\\main\\resources\\NSEHolidays.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ArrayList<LocalDate> holidays = new ArrayList<LocalDate>();
        while (s.hasNext()){
            String readLine = s.next();
            readLine = (readLine.charAt(readLine.length() - 1) == ',') ? readLine.substring(0, readLine.length() - 1) : readLine;
            readLine= readLine.substring(1, readLine.length()-1);
            holidays.add(LocalDate.parse(readLine));
        }
        s.close();
        return holidays;
    }

    public List<LocalDate> getBusinessDays(LocalDate startDate, LocalDate endDate, List<LocalDate> holidays) {
        if(endDate.compareTo(startDate)< 0)
            return null;
        List<LocalDate> businessDays= Stream.iterate(startDate,date-> date.plusDays(1))
                .limit(DAYS.between(startDate, endDate))
                .filter(day-> !(day.getDayOfWeek() == DayOfWeek.SATURDAY) && !(day.getDayOfWeek() == DayOfWeek.SUNDAY))
                .filter(day-> holidays== null? true: !holidays.contains(day) )
                .collect(Collectors.toList());
        System.out.println(businessDays);
        return businessDays;
    }


    DateTimeFormatter df = DateTimeFormatter.ofPattern("d-M-yyyy");

    // get response for a given date
    // https://www1.nseindia.com/ArchieveSearch?h_filetype=eqbhav&date=15-11-2022&section=EQ
    public void getBhavCopy(LocalDate day){
        try {
            Map<String, String> map= new HashMap<>();
            map.put("Accept", "*/*");
            map.put("Accept-Language", "en-US,en;q=0.5");
            map.put("Connection", "keep-alive");
            map.put("Upgrade-Insecure-Requests", "1");
            map.put("Host", "www1.nseindia.com");
            map.put("Referer", "https://www1.nseindia.com/products/content/equities/equities/archieve_eq.htm");
            map.put("Sec-GPC", "1");
            map.put("X-Requested-With", "XMLHttpRequest");
            String savedFileName = "cm".concat(day.getDayOfMonth()<10?"0".concat(String.valueOf(day.getDayOfMonth())): String.valueOf(day.getDayOfMonth())).concat(day.getMonth().toString().substring(0, 3)).concat(String.valueOf(day.getYear())).concat("bhav.csv.zip");
            String url= String.format("https://www1.nseindia.com/content/historical/EQUITIES/%s/%s/%s", day.getYear(), day.getMonth().toString().substring(0, 3), savedFileName);
            byte[] bytes= IOUtils.toByteArray(new URL(url));
            File file = new File("C:\\Users\\svjra\\Documents\\git\\Springboot\\three_candles\\zipData\\".concat(savedFileName));
            file.getParentFile().mkdirs(); // Will create parent directories if not exists
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            try {
                if (!savedFileName.endsWith(".zip")){
                    System.out.println(String.format("Issue with data in %s", day));
                    return;
                }
                fos.write(bytes);
                fos.close();

                String sourceFile = "C:\\Users\\svjra\\Documents\\git\\Springboot\\three_candles\\zipData\\".concat(savedFileName);
                String destinationFile = "C:\\Users\\svjra\\Documents\\git\\Springboot\\three_candles\\csvData\\";

                Unzip unzipper = new Unzip();
                unzipper.setSrc(new File(sourceFile));
                unzipper.setDest(new File(destinationFile));
                unzipper.execute();
            } catch (IOException e) {
                System.err.println("Could not read the file at '" + day + ":"+e.getMessage());
                fos.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
