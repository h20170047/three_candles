package com.svj.service;

import com.svj.dto.TradeSetupResponseDTO;
import com.svj.exception.FileException;
import com.svj.exception.StockProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static com.svj.utils.AppUtils.dateFormatter;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NSEServiceTest {

    private NSEService service;

    @BeforeEach
    public void setup(){
        service = new NSEService("src/test/resources/data/",
                "src/main/resources/Nifty50List.txt",
                "src/test/resources/NSEHolidays.txt",
                3);
    }

    @Test
    public void getStocksList_BullsAndBears(){
        TradeSetupResponseDTO result = service.getStocksList(LocalDate.parse("26-11-2022", dateFormatter));
        assertThat(result.getBullish()).isNotEmpty();
        assertThat(result.getBearish()).isNotEmpty();
    }

    @Test
    public void getStocksList_MissingNecessaryData(){
        assertThrows(StockProcessingException.class, ()-> service.getStocksList(LocalDate.parse("1-12-2022", dateFormatter)));
    }

    @Test
    public void getBusinessDays(){
        LocalDate startDate= LocalDate.parse("08-11-2022", dateFormatter);
        LocalDate endDate= LocalDate.parse("15-11-2022", dateFormatter);
        List<LocalDate> businessDays= service.getBusinessDays(startDate, endDate);
        assertThat(businessDays.size()).isEqualTo(4);
    }

    @Test
    public void getBusinessDays_invalidParameters(){
        LocalDate startDate= LocalDate.parse("8-11-2022", dateFormatter);
        LocalDate endDate= LocalDate.parse("7-11-2022", dateFormatter);
        List<LocalDate> businessDays= service.getBusinessDays(startDate, endDate);
        assertThat(businessDays).isNull();
    }

    @Test
    @Disabled
    public void getNSEData(){
        LocalDate startDate= LocalDate.parse("19-11-2022", dateFormatter);
        LocalDate endDate= LocalDate.parse("24-11-2022", dateFormatter);
        List<LocalDate> businessDays = service.getBusinessDays(startDate, endDate);
        for(LocalDate day: businessDays){
            service.getBhavCopy(day);
        }
    }

    @Test
    public void getHolidays(){
        List<LocalDate> holidays = service.getHolidayList(service.getHolidayFilePath());
        assertThat(holidays.size()).isEqualTo(346);
    }

    @Test
    public void getNiftyStocks(){
        List<String> holidays = service.getDataFromFile(service.getNiftyFilePath());
        assertThat(holidays.size()).isEqualTo(50);
    }

    @Test
    public void getDataFromFile_missingFile(){
        assertThrows(FileException.class, ()-> service.getDataFromFile("invalid path"));
    }

    @Test
    public void getFileNameFromDate_PrefixZero(){
        String fileName = service.getFileNameFromDate(LocalDate.parse("9-11-2022", dateFormatter));
        assertThat(fileName).isEqualTo("cm09NOV2022bhav.csv");
    }

    @Test
    public void getFileNameFromDate_normal(){
        String fileName = service.getFileNameFromDate(LocalDate.parse("19-4-2022", dateFormatter));
        assertThat(fileName).isEqualTo("cm19APR2022bhav.csv");
    }
}