package com.svj.service;

import com.svj.entity.Stock;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TechnicalIndicatorsTest {
    @Test
    public void narrowCPR(){
        TechnicalIndicators indicators= new TechnicalIndicators();
        Stock stock= new Stock("narrowCPR_stock", 100, 110, 90, 90, LocalDateTime.now());
        boolean narrowCPR = indicators.narrowCPR(stock);
        assertThat(narrowCPR).isTrue();
    }

    @Test
    public void wideCPR(){
        TechnicalIndicators indicators= new TechnicalIndicators();
        Stock stock= new Stock("wideCPR_stock", 100, 150, 60, 90, LocalDateTime.now());
        boolean narrowCPR = indicators.narrowCPR(stock);
        assertThat(narrowCPR).isFalse();
    }

    @Test
    @Disabled
    public void calcCPR(){
        TechnicalIndicators indicators= new TechnicalIndicators();
        Stock stock= new Stock("unknownStock", 838.05, 840, 822, 822, LocalDateTime.now());
        boolean narrowCPR = indicators.narrowCPR(stock);
        assertThat(narrowCPR).isFalse();
    }

}