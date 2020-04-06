package com.litmos.gridu.ilyavy.analyzer;

import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.litmos.gridu.ilyavy.analyzer.githubapi.GithubService;
import com.litmos.gridu.ilyavy.analyzer.service.AccountsConsumer;
import com.litmos.gridu.ilyavy.analyzer.service.CommitsProducer;
import com.litmos.gridu.ilyavy.analyzer.service.IntervalDeserializer;

/** Main class of the application. */
public class AccountsAnalyzerApp {

    private static final Logger logger = LoggerFactory.getLogger(AccountsAnalyzerApp.class);

    private static final String GROUP_ID = "github-accounts-analyzer";

    private static final String INPUT_TOPIC = "github-accounts";

    private static final Duration CONSUMER_POLLING_TIMEOUT = Duration.ofMillis(1000);

    private static final String GITHUB_API_DEFAULT_BASE_URL = "https://api.github.com/";

    private static final String OUTPUT_TOPIC = "github-commits";

    private static final int SHUTDOWN_TIMEOUT_MS = 1000;

    private static volatile boolean shutdownFlag = false;

    private AccountsConsumer accountsConsumer;

    private CommitsProducer commitsProducer;

    private GithubService githubService;

    private IntervalDeserializer intervalDeserializer;

    public AccountsAnalyzerApp(String bootstrapServers, String githubApiBaseUrl) {
        intervalDeserializer = new IntervalDeserializer();
        githubService = new GithubService(githubApiBaseUrl);

        accountsConsumer = new AccountsConsumer(bootstrapServers, GROUP_ID).subscribe(INPUT_TOPIC);
        commitsProducer = new CommitsProducer(bootstrapServers, OUTPUT_TOPIC);

        addShutdownHook();
    }

    public AccountsAnalyzerApp(String bootstrapServers) {
        this(bootstrapServers, GITHUB_API_DEFAULT_BASE_URL);
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdownFlag = true;
            try {
                Thread.sleep(SHUTDOWN_TIMEOUT_MS);
            } catch (InterruptedException e) {
                logger.warn("Error occurred when shutting down", e);
            }
        }));
    }

    public void runPipeline() {
        accountsConsumer.poll(CONSUMER_POLLING_TIMEOUT)
                .flatMap(account -> {
                    LocalDateTime startingFrom = intervalDeserializer.countStartingDateTime(account.getInterval());
                    return githubService.pollCommits(account.getAccount(), startingFrom);
                })
                .subscribe(commitsProducer::send, e -> logger.warn("Processing error occurred", e));
    }

    public void close() {
        logger.info("Shutting down...");
        accountsConsumer.close();
        commitsProducer.close();
    }

    public static void main(String[] args) {
        AccountsAnalyzerApp app = new AccountsAnalyzerApp("localhost:9092,localhost:9095,localhost:9098");

        try {
            while (!shutdownFlag) {
                app.runPipeline();
            }
        } finally {
            app.close();
        }
    }
}
