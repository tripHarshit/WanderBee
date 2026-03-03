package com.wanderbee.destinationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

import java.util.TimeZone;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableCaching
public class DestinationServiceApplication {

    // Force JVM timezone to UTC before JDBC driver reads the system timezone.
    // Without this, Windows maps "India Standard Time" -> "Asia/Calcutta" (a
    // deprecated alias), which PostgreSQL 14+ rejects with a FATAL error.
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    public static void main(String[] args) {
        SpringApplication.run(DestinationServiceApplication.class, args);
    }

}
