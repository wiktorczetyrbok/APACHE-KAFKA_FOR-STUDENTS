package com.litmos.gridu.ilyavy.analyzer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.litmos.gridu.ilyavy.analyzer.model.Account;
import com.litmos.gridu.ilyavy.analyzer.service.AccountsConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static final String BOOTSTRAP_SERVERS = "localhost:9092,localhost:9095,localhost:9098";

    private static final String GROUP_ID = "github-accounts-analyzer";

    private static final String INPUT_TOPIC = "github-accounts";

    private static final Duration CONSUMER_POLLING_TIMEOUT = Duration.ofMillis(1000);

    private static final String OUTPUT_TOPIC = "github-commits";

    private static final int SHUTDOWN_TIMEOUT_MS = 1000;

    private static volatile boolean shutdownFlag = false;

    public static void main(String[] args) throws JsonProcessingException {
        AccountsConsumer accountsConsumer = new AccountsConsumer(BOOTSTRAP_SERVERS, INPUT_TOPIC, GROUP_ID);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdownFlag = true;
            try {
                Thread.sleep(SHUTDOWN_TIMEOUT_MS);
            } catch (InterruptedException e) {
                logger.warn("Error occurred when shutting down", e);
            }
        }));

        try {
            while (!shutdownFlag) {
                List<Account> accounts = accountsConsumer.poll(CONSUMER_POLLING_TIMEOUT);

            }
        } finally {
            logger.info("Shutting down...");
            accountsConsumer.close();
        }
    }
}
