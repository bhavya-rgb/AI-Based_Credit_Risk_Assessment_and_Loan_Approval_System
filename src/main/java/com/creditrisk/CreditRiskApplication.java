package com.creditrisk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CreditRiskApplication {

    public static void main(String[] args) {
        SpringApplication.run(CreditRiskApplication.class, args);
    }
}
