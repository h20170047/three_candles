package com.svj.repository;

import com.svj.entity.StockDayData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface StockDayPriceRepository extends MongoRepository<StockDayData, String> {
    List<StockDayData> findBySYMBOL(String symbol);
}
