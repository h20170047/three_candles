package com.svj.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeSetupResponseDTO {
    private LocalDate tradeDate;
    private List<String> bullish;
    private List<String> bearish;
}
