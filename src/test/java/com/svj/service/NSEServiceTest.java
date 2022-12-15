package com.svj.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.svj.utils.AppUtils.dateFormatter;

class NSEServiceTest {
    @Test
    @Disabled
    public void getNSEData(){
        StocksProcessor stocksProcessor= new StocksProcessor("data/",
                "Nifty50List.txt",
                "NSEHolidays.txt",
                3,
                new NSEService("save/", "target/test-classes/data"));
        NSEService service= new NSEService("save/", "target/test-classes/data");
        LocalDate startDate= LocalDate.parse("13-12-2022", dateFormatter);
        LocalDate endDate= LocalDate.parse("16-12-2022", dateFormatter);
        List<LocalDate> businessDays = stocksProcessor.getBusinessDays(startDate, endDate);
        for(LocalDate day: businessDays){
            service.getBhavCopy(day);
        }
    }

}