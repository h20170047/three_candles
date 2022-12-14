package com.svj.service;

import com.svj.entity.Stock;

public class TechnicalIndicators {
    public static boolean narrowCPR(Stock stock){
        double pivot= (stock.getDayHigh()+stock.getDayLow()+stock.getClose())/3;
        double top= (stock.getDayHigh()+stock.getDayLow())/2;
        double bottom=2*pivot-top;
        if(Math.abs(top-bottom)<stock.getClose()*0.001) //wider CPR is considered when CPR range to be less than .1% of close
            return true;
        else
            return false;
    }
}
