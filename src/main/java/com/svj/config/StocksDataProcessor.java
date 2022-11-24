package com.svj.config;

import com.svj.entity.StockDayData;
import org.springframework.batch.item.ItemProcessor;

public class StocksDataProcessor implements ItemProcessor<StockDayData, StockDayData> {


    @Override
    public StockDayData process(StockDayData stockData) throws Exception {
        if(stockData.getSERIES().equals("EQ")){
            return stockData;
        }
        return null;
    }


}
