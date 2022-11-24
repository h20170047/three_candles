package com.svj.config;

import com.svj.repository.StockDayPriceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.*;

class ApplicationBatchConfigTest {

    @MockBean
    private StockDayPriceRepository repository;

    @MockBean
    private StepBuilderFactory stepBuilderFactory;

    @MockBean
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    static ResourceLoader resourceLoader;

    @Value("classpath:input/*.csv")
    private Resource[] resources;

    @Test
    void itemReader() {
//        ApplicationBatchConfig config= new ApplicationBatchConfig();
//        config.itemReader();
//        Arrays.stream(ApplicationBatchConfig.resources).forEach(r-> System.out.println(r.getFilename()));
//
//        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
//                                        .parseCaseInsensitive()
//                                        .appendPattern("dd-MMM-yyyy")
//                                        .toFormatter();
//        LocalDate date = LocalDate.parse("02-NOV-2022", formatter);
//        System.out.println(date);

        ClassLoader classLoader = this.getClass().getClassLoader();
        System.out.println(resources);
    }
}