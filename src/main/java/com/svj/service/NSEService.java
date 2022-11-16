package com.svj.service;

import com.svj.entity.Stock;
import com.svj.exception.NSEDataProcessingException;
import com.svj.utils.AppUtils;
import org.apache.ant.compress.taskdefs.Unzip;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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

    public String getDataForDay() {
        HttpHeaders headers = new HttpHeaders();
//        headers.set("User-Agent", "PostmanRuntime/7.29.2");
        headers.set("Accept", "*/*");

//        headers.set("Accept-Encoding", "gzip, deflate, br");
        headers.set("Accept-Language", "en-US,en;q=0.5");
        headers.set("Connection", "keep-alive");
//        headers.set("Dnt", "1");
        headers.set("Upgrade-Insecure-Requests", "1");
        headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36");
        headers.set("Host", "www1.nseindia.com");
        headers.set("Referer", "https://www1.nseindia.com/products/content/equities/equities/eq_security.htm");
        headers.set("Sec-GPC", "1");
        headers.set("X-Requested-With", "XMLHttpRequest");
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseTxt = restTemplate.exchange(
//                "https://www.nseindia.com/api/equity-stockIndices?index=NIFTY 50"
                "https://www1.nseindia.com/products/dynaContent/common/productsSymbolMapping.jsp?symbol=INFY&segmentLink=3&symbolCount=1&series=ALL&dateRange=day&fromDate=&toDate=&dataType=PRICEVOLUMEDELIVERABLE"
                , HttpMethod.GET, requestEntity, String.class, headers);

        try {
            String blogUrl = "https://www1.nseindia.com/products/dynaContent/common/productsSymbolMapping.jsp?symbol=INFY&segmentLink=3&symbolCount=1&series=ALL&dateRange=day&fromDate=&toDate=&dataType=PRICEVOLUMEDELIVERABLE";
            Map<String, String> map= new HashMap<>();
            map.put("Accept", "*/*");
            map.put("Accept-Language", "en-US,en;q=0.5");
            map.put("Connection", "keep-alive");
            map.put("Upgrade-Insecure-Requests", "1");
            map.put("Host", "www1.nseindia.com");
            map.put("Referer", "https://www1.nseindia.com/products/content/equities/equities/eq_security.htm");
            map.put("Sec-GPC", "1");
            map.put("X-Requested-With", "XMLHttpRequest");
            Document doc = Jsoup.connect(blogUrl).headers(map).get();

            ArrayList<String> downServers = new ArrayList<>();
            Element table = doc.select("table").get(0); //select the first table.
            Elements rows = table.select("tr");

            List<Stock> stocks= new LinkedList<>();
            NumberFormat numberFormat= NumberFormat.getNumberInstance();
            for (int i = 1; i < rows.size(); i++) { //first row is the col names so skip it. 0-Symbol, 1-Series, 2-Date, 3-Prev Close, 4-Open Price, 5-High Price, 6-Low Price, 7-Close Price, 8-VWAP, 9-Total Traded Quantity,
                                                    // 10-TurnOver, 11-No. of Trades, 12-Deliverable Qty, 13-% Dly Qt to Traded Qty
                Element row = rows.get(i);
                Elements cols = row.select("td");

                stocks.add(new Stock(cols.get(0).text(), numberFormat.parse(cols.get(7).text()).doubleValue(), numberFormat.parse(cols.get(5).text()).doubleValue(), numberFormat.parse(cols.get(6).text()).doubleValue(), numberFormat.parse(cols.get(4).text()).doubleValue(), LocalDateTime.parse(cols.get(2).text(), dateTimeFormatter)));
            }
            System.out.println(stocks);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return responseTxt.getBody().toString();
    }

    public List<Stock> convertTextToStocks(String jsonTxt) {
        List<Stock> dailyData= new LinkedList<>();
        try{
            JSONObject json = new JSONObject(jsonTxt);
            JSONArray stockArray = json.getJSONArray("data");
            for(int i=0; i<stockArray.length(); i++){
                JSONObject o = (JSONObject) stockArray.get(i);
                Stock stock= AppUtils.convertToStockEntity(o);
                dailyData.add(stock);
            }
        return dailyData;
        } catch (JSONException e) {
            throw new NSEDataProcessingException(e.getMessage());
        }
    }

    public List<Stock> processData() {
        String dataString= getDataForDay();
        List<Stock> stockList= convertTextToStocks(dataString);
        for(Stock stock: stockList){
            System.out.println(stock.getSymbol());
        }
        return stockList;
    }

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
            // reject= 1, bearish= 2, bullish= 3
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
            byte[] bytes = Jsoup.connect(String.format("https://www1.nseindia.com/content/historical/EQUITIES/%s/%s/%s", day.getYear(), day.getMonth().toString().substring(0, 3), savedFileName))
                    .headers(map)
                    .ignoreContentType(true)
                    .timeout(600000)
                    .execute()
                    .bodyAsBytes();
            try {
                if (!savedFileName.endsWith(".zip")){
                    System.out.println(String.format("Issue with data in %s", day));
                    return;
                }
                File file = new File("C:\\Users\\svjra\\Documents\\git\\Springboot\\three_candles\\zipData\\".concat(savedFileName));
                file.getParentFile().mkdirs(); // Will create parent directories if not exists
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file );
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
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
