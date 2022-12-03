package com.svj.controller;

import com.svj.dto.ServiceResponse;
import com.svj.dto.TradeSetupResponseDTO;
import com.svj.service.NSEService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {
    private NSEService service;

    public void AnalysisController(NSEService nseService){
        service= nseService;
    }

    @RequestMapping("/trade-setup/{tradeDate}")
    // input date, and take necessary files from common pool.
    // if required files are missing, return corresponding error message
    public ServiceResponse getTradeSetup(@PathVariable LocalDate tradeDate){
        log.info("AnalysisController:getTradeSetup Received request with tradeDate= {}", tradeDate.toString());
        TradeSetupResponseDTO stocksList = service.getStocksList(tradeDate);
        ServiceResponse response = new ServiceResponse(HttpStatus.OK, stocksList, null);
        log.info("AnalysisController:getTradeSetup Response {}", response);
        return response;
    }

}
