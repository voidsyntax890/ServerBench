package com.serverbench.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ServerBenchAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                ServerBenchAgentApplication.class,
                args
        );
    }
}
