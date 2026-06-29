package com.cropwise.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CropWise AI backend entry point.
 * Owns authentication + data (JWT, JPA) and proxies the external
 * AI / weather / market / audio APIs the website used directly.
 */
@SpringBootApplication
public class CropWiseApplication {
    public static void main(String[] args) {
        SpringApplication.run(CropWiseApplication.class, args);
    }
}
