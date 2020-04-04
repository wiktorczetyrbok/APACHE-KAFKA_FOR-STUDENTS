package com.litmos.gridu.ilyavy.analyzer;

import java.time.Duration;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.litmos.gridu.ilyavy.analyzer.githubapi.GithubService;
import com.litmos.gridu.ilyavy.analyzer.service.AccountsConsumer;
import com.litmos.gridu.ilyavy.analyzer.service.CommitsProducer;
import com.litmos.gridu.ilyavy.analyzer.service.IntervalDeserializer;

/** Main class of the application. */
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
        GithubService githubService = new GithubService();
        IntervalDeserializer intervalDeserializer = new IntervalDeserializer();

        AccountsConsumer accountsConsumer = new AccountsConsumer(BOOTSTRAP_SERVERS, GROUP_ID).subscribe(INPUT_TOPIC);
        CommitsProducer commitsProducer = new CommitsProducer(BOOTSTRAP_SERVERS, OUTPUT_TOPIC);

        addShutdownHook();

        try {
            while (!shutdownFlag) {
                accountsConsumer.poll(CONSUMER_POLLING_TIMEOUT)
                        .flatMap(account -> {
                            LocalDateTime startingFrom = intervalDeserializer.countStartingDateTime(account.getInterval());
                            return githubService.pollCommits(account.getAccount(), startingFrom);
                        })
                        .subscribe(commitsProducer::send, e -> logger.warn("Processing error occurred", e));
            }
        } finally {
            logger.info("Shutting down...");
            accountsConsumer.close();
            commitsProducer.close();
        }
    }

    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdownFlag = true;
            try {
                Thread.sleep(SHUTDOWN_TIMEOUT_MS);
            } catch (InterruptedException e) {
                logger.warn("Error occurred when shutting down", e);
            }
        }));
    }
}
