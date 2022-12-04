package com.svj.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.svj.exception.ApplicationGlobalExceptionHandler;
import com.svj.service.NSEService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
    public ObjectMapper objectMapper;
    private final String uri= "/analysis";

    @BeforeEach
    public void setUp(){
        objectMapper= new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc= MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new ApplicationGlobalExceptionHandler())
                .build();
    }

    @Test
    public void getTradeSetupTest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(uri+"/trade-setup/{tradeDay}", "26-11-2022")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response.bullish").isArray());
    }

    @Test
    public void getTradeSetup_invalidDate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(uri+"/trade-setup/{tradeDay}", "50-11-2022")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getTradeSetup_SQLInjection() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(uri+"/trade-setup/{tradeDay}", "SELECT * FROM DB")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getTradeSetup_whenDataIsMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(uri+"/trade-setup/{tradeDay}", "1-12-2022")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is5xxServerError());
    }


}
