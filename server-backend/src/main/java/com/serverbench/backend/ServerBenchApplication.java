package com.serverbench.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerBenchApplication {

    public static void main(String[] args) {

        SpringApplication.run(
                ServerBenchApplication.class,
                args
        );
    }
}