package com.svj.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;

@Slf4j
public class BatchStepEventListener implements SkipListener {
    ObjectMapper objectMapper= new ObjectMapper();

    @Override
    public void onSkipInRead(Throwable throwable) {
        log.info("A failure on read {}", throwable.getMessage());
    }

    @Override
    public void onSkipInWrite(Object o, Throwable throwable) {
        log.info("A failure in write: {}",throwable.getMessage());
    }

    @Override
    public void onSkipInProcess(Object o, Throwable throwable) {
        log.info("Customer value: {}, error: {}",writeJson(o), throwable.getMessage());
    }

    private String writeJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
