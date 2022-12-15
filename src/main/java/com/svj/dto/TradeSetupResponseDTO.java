package com.svj.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.svj.entity.CPRWidth;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
public class TradeSetupResponseDTO {
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "d-M-yyyy")
    private LocalDate tradeDate;
    private List<String> bullish;
    private List<String> bearish;
    private List<CPRWidth> trending;
}
