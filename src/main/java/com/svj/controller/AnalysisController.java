package com.svj.controller;

import com.svj.dto.ServiceResponse;
import com.svj.dto.TradeSetupResponseDTO;
import com.svj.service.NSEService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {
    private NSEService service;

    public AnalysisController(NSEService nseService){
        service= nseService;
    }

    @GetMapping("/trade-setup/{tradeDate}")
    // input date, and take necessary files from common pool.
    // if required files are missing, return corresponding error message
    @Operation(summary = "Get screened list of stocks for a given day")
    @ApiResponses(value={
            @ApiResponse(responseCode ="200", description="list of bullish and bearish stocks are returned", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = TradeSetupResponseDTO.class))
            }),
            @ApiResponse(responseCode ="400", description="Entered inputs are not valid"),
            @ApiResponse(responseCode ="500", description="Some error at server side")
    })
    public ServiceResponse getTradeSetup(@PathVariable @DateTimeFormat(pattern="d-M-yyyy") LocalDate tradeDate){
        log.info("AnalysisController:getTradeSetup Received request with tradeDate= {}", tradeDate.toString());
        TradeSetupResponseDTO stocksList = service.getStocksList(tradeDate);
        ServiceResponse response = new ServiceResponse(HttpStatus.OK, stocksList, null);
        log.info("AnalysisController:getTradeSetup Response {}", response);
        return response;
    }

}
