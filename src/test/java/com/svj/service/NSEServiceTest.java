package com.svj.service;

import com.svj.dto.TradeSetupResponseDTO;
import com.svj.exception.FileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NSEServiceTest {

    private NSEService service;

    @BeforeEach
    public void setup(){
        service = new NSEService("src/test/resources/data/");
//        service = new NSEService("C:\\Users\\svjra\\Documents\\git\\Springboot\\three_candles\\src\\main\\resources\\data\\");
    }

    @Test
    public void getStocksList_BullsAndBears(){
        TradeSetupResponseDTO result = service.getStocksList(LocalDate.parse("26-11-2022", df));
        assertThat(result.getBullish()).isNotEmpty();
        assertThat(result.getBearish()).isNotEmpty();
    }

    @Test
    public void getStocksList_MissingNecessaryData(){
        assertThrows(FileException.class, ()-> service.getStocksList(LocalDate.parse("1-12-2022", df)));
    }

    @Test
    public void getOldDataTest(){
        service.getOldData();
    }

    DateTimeFormatter df = DateTimeFormatter.ofPattern("d-M-yyyy");
    @Test
    public void getBusinessDays(){
        LocalDate startDate= LocalDate.parse("08-11-2022", df);
        LocalDate endDate= LocalDate.parse("15-11-2022", df);
        List<LocalDate> holidays= Arrays.asList(LocalDate.parse("8-11-2022", df));
        List<LocalDate> businessDays= service.getBusinessDays(startDate, endDate, holidays);
        assertThat(businessDays.size()).isEqualTo(4);
    }

    @Test
    public void getNSEData(){
        LocalDate startDate= LocalDate.parse("19-11-2022", df);
        LocalDate endDate= LocalDate.parse("24-11-2022", df);
        List<LocalDate> holidays= service.getHolidays();
        List<LocalDate> businessDays = service.getBusinessDays(startDate, endDate, holidays);
        for(LocalDate day: businessDays){
            service.getBhavCopy(day);
        }
    }
}