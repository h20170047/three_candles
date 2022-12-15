package com.svj.service;

import com.svj.entity.CPRWidth;
import com.svj.entity.Stock;

public class TechnicalIndicators {
    public static CPRWidth narrowCPR(Stock stock){
        double pivot= (stock.getDayHigh()+stock.getDayLow()+stock.getClose())/3;
        double top= (stock.getDayHigh()+stock.getDayLow())/2;
        double bottom=2*pivot-top;
        double cprWidth= Math.abs(top-bottom)/stock.getClose();
        return new CPRWidth(stock.getSymbol(), String.format("%.4f", cprWidth), Math.abs(top-bottom)<stock.getClose()*0.001);
    }
}
