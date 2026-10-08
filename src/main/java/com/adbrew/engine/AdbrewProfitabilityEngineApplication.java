package com.adbrew.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AdbrewProfitabilityEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdbrewProfitabilityEngineApplication.class, args);
    }
}
