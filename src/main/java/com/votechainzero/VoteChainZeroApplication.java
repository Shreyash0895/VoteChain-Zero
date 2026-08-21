package com.votechainzero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VoteChainZeroApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoteChainZeroApplication.class, args);
    }

}