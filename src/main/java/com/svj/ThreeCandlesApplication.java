package com.svj;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "3-CANDLES SERVICE", version = "v3.0", description = "3-candles API services helps in screening stocks and journaling executed trades"))
public class ThreeCandlesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThreeCandlesApplication.class, args);
	}

}
