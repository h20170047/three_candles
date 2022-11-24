package com.svj.controller;

import com.svj.service.StockHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/history")
public class StockHistory {
    private StockHistoryService service;

    @Autowired
    public StockHistory(StockHistoryService service){
        this.service= service;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<Resource> getCSV(@PathVariable String symbol){
        InputStreamResource file = new InputStreamResource(service.load(symbol));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + symbol + ".csv")
                .contentType(MediaType.parseMediaType("application/csv"))
                .body(file);
    }
}
