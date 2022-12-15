package com.svj.service;

import com.svj.dto.TradeSetupResponseDTO;
import com.svj.entity.CPRWidth;
import com.svj.entity.Stock;
import com.svj.exception.FileException;
import com.svj.exception.StockProcessingException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

import static com.svj.service.TechnicalIndicators.narrowCPR;
import static com.svj.utils.AppUtils.getFileNameFromDate;
import static com.svj.utils.AppUtils.getResourceFileAsStringList;
import static com.svj.utils.Constants.*;
import static java.time.temporal.ChronoUnit.DAYS;

@Service
@Data
@Slf4j
public class StocksProcessor {
    private String dataPath;
    private String niftyFilePath;
    private String holidayFilePath;
    private List<LocalDate> holidays;
    private int candleCount;
    DateTimeFormatter dateTimeFormatter;
    private NSEService NService;

    public StocksProcessor(@Value("${nse.data.bhavcopy}")String dataPath,
                           @Value("${nse.data.nifty50}")String niftyFilePath,
                           @Value("${nse.data.holiday}")String holidayFilePath,
                           @Value("${strategy.countOfCandlesConsidered}")Integer candleCount,
                           NSEService NService){
        this.dataPath= dataPath;
        this.niftyFilePath = niftyFilePath;
        this.holidayFilePath = holidayFilePath;
        this.candleCount= candleCount;
        dateTimeFormatter= new DateTimeFormatterBuilder()
                        .parseCaseInsensitive()
                        .appendPattern("dd-MMM-yyyy[ [HH][:mm][:ss][.SSS]]")
                        .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                        .toFormatter();
        this.NService= NService;
    }


    public TradeSetupResponseDTO getStocksList(LocalDate tradeDay) {
        log.info("NSEService:getStocksList Method execution started");
        try{
            TradeSetupResponseDTO result= new TradeSetupResponseDTO();
            result.setTradeDate(tradeDay);
            // get last 3 trading days from i/p
            // read files to m/y and process
            // in case required data is not present o/p valid error message
            List<LocalDate> holidays = getHolidayList(holidayFilePath);
            List<Path> dataList= new LinkedList<>();
            LocalDate processingDay= tradeDay.plusDays(-1); // get analysis from last 3 days
            while(dataList.size()<candleCount){
                // add file to dataList only if day is working
                if(processingDay.getDayOfWeek()!= DayOfWeek.SATURDAY && processingDay.getDayOfWeek()!= DayOfWeek.SUNDAY){
                    if(!holidays.contains(processingDay)){
                        log.debug("NSEService:getStocksList Identified business days prior to tradeDay for analysis is {}", processingDay.toString());
                        dataList.add(Paths.get(dataPath.concat(getFileNameFromDate(processingDay)))); // note that previous day's data is inserted first
                        NService.getBhavCopy(processingDay); // add relevant file from NSE portal
                    }
                }
                processingDay= processingDay.plusDays(-1);
            }
            // create map and list for 2 categories- bullish, bearish
            // get 2weeks data from the nse1 api, get the last 3 days info from today, if it is market-hours. else consider today too
            // return response map data
            // considering only NIFTY 50 stocks
            List<String> nifty50Stocks= getResourceFileAsStringList(niftyFilePath);
            Map<String, List<Stock>> threeDaysInfo= new HashMap<>();
            List<String> bullish= new LinkedList<>();
            List<String> bearish= new LinkedList<>();
            List<CPRWidth> trending= new LinkedList<>();
            dataList.stream()
                .forEach(file->{
                    System.out.println("Processing "+file.getFileName());
                    try{
                        getResourceFileAsStringList(file.toString()).stream()
                                .skip(1)
                                .filter(line-> line.split(",")[ONE].equals("EQ"))
                                .filter(line-> nifty50Stocks.contains(line.split(",")[ZERO])) // if stock if nifty50, save to list
                                .map(line-> line.split(","))
                                .map(stringArray-> new Stock(stringArray[ZERO], Double.parseDouble(stringArray[FIVE]), Double.parseDouble(stringArray[THREE]), Double.parseDouble(stringArray[FOUR]), Double.parseDouble(stringArray[TWO]), LocalDateTime.parse(stringArray[TEN], dateTimeFormatter))) // Pulling out necessary details
                                .forEach(stock-> {
                                    List<Stock> currList= threeDaysInfo.getOrDefault(stock.getSymbol(), new LinkedList<>());
                                    currList.add(stock);
                                    threeDaysInfo. put(stock.getSymbol(), currList);
                                });
                    }catch (FileException ex){
                        log.error("Missing {} file in classpath", file.getFileName());
                        throw new StockProcessingException(ex.getMessage());
                        // get file from server
                    }

                });
            for(String stock: threeDaysInfo.keySet()){
                List<Stock> data= threeDaysInfo.get(stock);
                boolean isBullish= data.get(ZERO).getOpen()< data.get(ZERO).getClose(); //Consider stock's behavior with first candle
                int i= ONE; // process from next candle wrt first candle
                for(; i<candleCount; i++){
                    if(data.get(i).getOpen()< data.get(i).getClose()!= isBullish){ // checking behavior of stock's i-th candle with its first candle
                        i= TEN;
                    }
                }
                if(i==candleCount){ // This stock has similar behavior as its first candle. Categorize it into either bullish or bearish bucket
                    if(isBullish)
                        bullish.add(stock);
                    else
                        bearish.add(stock);
                }
                // analyse the latest day's CPR to check if it is trending for trade day. Last day is inserted first
                ;
                CPRWidth stockCPR = narrowCPR(data.get(0));
                if(stockCPR.isNarrowCPR())  //check for CPR trend on all Nifty50 stocks
                    trending.add(stockCPR);
            }
            Collections.sort(trending, Comparator.comparing(CPRWidth::getCprWidth));
            result.setBullish(bullish);
            result.setBearish(bearish);
            result.setTrending(trending);
            log.info("NSEService:getStocksList Method execution completed");
            return result;
        }catch (Exception e){
            log.error("NSEService:getStocksList Exception occurred while getting stocks filtering- {}", e.getMessage());
            throw new StockProcessingException(e.getMessage());
        }
    }

    public List<LocalDate> getHolidayList(String filePath) {
        List<String> holidaysText= getResourceFileAsStringList(filePath);
        ArrayList<LocalDate> holidays = new ArrayList<LocalDate>();
        holidaysText.stream()
                .forEach(holiday-> holidays.add(LocalDate.parse(holiday)));
        return holidays;
    }

    public List<LocalDate> getBusinessDays(LocalDate startDate, LocalDate endDate) {
        if(holidays== null)
            holidays= getHolidayList(holidayFilePath);
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
}
