package com.svj.repository;

import com.svj.entity.StockDayData;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StockDayPriceRepository extends MongoRepository<StockDayData, String> {
}
