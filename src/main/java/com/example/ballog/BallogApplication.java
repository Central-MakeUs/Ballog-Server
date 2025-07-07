package com.example.ballog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BallogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BallogApplication.class, args);
    }

}
