package com.votechainzero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * VoteChain Zero
 * A zero-error blockchain-based voting system.
 *
 * Entry point for the Spring Boot application.
 */
@SpringBootApplication
@EnableScheduling // needed later for periodic chain-integrity checks
public class VoteChainZeroApplication {

    public static void main(String[] args) {
        SpringApplication.run(VoteChainZeroApplication.class, args);
    }

}
