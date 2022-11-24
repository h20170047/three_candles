package com.svj.service;

import com.svj.entity.StockDayData;
import com.svj.repository.StockDayPriceRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class StockHistoryService {
    private StockDayPriceRepository repository;

    @Autowired
    public void stockHistoryService(StockDayPriceRepository stockDayPriceRepository){
        repository= stockDayPriceRepository;
    }


    public List<StockDayData> getStockHistory(String symbol) {
        return repository.findBySYMBOL(symbol);
    }

    public ByteArrayInputStream load(String symbol) {

        ByteArrayInputStream in = generateReport(symbol);
        return in;
    }



    public ByteArrayInputStream generateReport(String symbol) {
        final CSVFormat format = CSVFormat.DEFAULT.withQuoteMode(QuoteMode.MINIMAL);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out), format);) {
            csvPrinter.printRecord(Arrays.asList("SYMBOL", "SERIES", "OPEN", "HIGH", "LOW", "CLOSE", "LAST", "PREVCLOSE", "TOTTRDQTY", "TOTTRDVAL", "TIMESTAMP"));
            List<StockDayData> stockDatas = getStockHistory(symbol);
            Collections.sort(stockDatas, Comparator.comparing(StockDayData::getTIMESTAMP));
            for (StockDayData stockData : stockDatas) {
                List<String> data = Arrays.asList(
                        stockData.getSYMBOL(),
                        stockData.getSERIES(),
                        String.valueOf(stockData.getOPEN()),
                        String.valueOf(stockData.getHIGH()),
                        String.valueOf(stockData.getLOW()),
                        String.valueOf(stockData.getCLOSE()),
                        String.valueOf(stockData.getLAST()),
                        String.valueOf(stockData.getPREVCLOSE()),
                        String.valueOf(stockData.getTOTTRDQTY()),
                        String.valueOf(stockData.getTOTTRDVAL()),
                        stockData.getTIMESTAMP().toString()
                );
                csvPrinter.printRecord(data);
            }

            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("failed to import data to CSV file: " + e.getMessage());
        }
    }
}
