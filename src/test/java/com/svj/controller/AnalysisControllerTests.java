package com.svj.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.svj.service.NSEService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
public class AnalysisControllerTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AnalysisController controller;
    @Autowired
    private NSEService service;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp(){

    }

}
