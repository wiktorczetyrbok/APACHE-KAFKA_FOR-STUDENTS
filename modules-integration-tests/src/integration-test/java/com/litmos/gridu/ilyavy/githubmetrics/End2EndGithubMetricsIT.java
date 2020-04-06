package com.litmos.gridu.ilyavy.githubmetrics;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.litmos.gridu.ilyavy.analyzer.AccountsAnalyzerApp;
import com.litmos.gridu.ilyavy.analyzer.model.Account;
import com.litmos.gridu.ilyavy.analyzer.model.Commit;
import com.litmos.gridu.ilyavy.metrics.streams.MetricsKafkaStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@RunWith(SpringRunner.class)
@DirtiesContext
public class End2EndGithubMetricsIT {

    private static final Logger logger = LoggerFactory.getLogger(End2EndGithubMetricsIT.class);

    private static final String TOTAL_COMMITS_NUMBER_TOPIC = "github-metrics-total-commits";

    private static final String COMMITTERS_NUMBER_TOPIC = "github-metrics-total-committers";

    private static final String TOP_COMMITTERS_TOPIC = "github-metrics-top-committers";

    private static final String USED_LANGUAGES_TOPIC = "github-metrics-languages";

    private static KafkaProducer<String, String> accountsProducer;

    private static KafkaConsumer<String, String> commitsConsumer;

    private static KafkaConsumer<String, String> totalCommitsMetricConsumer;

    private static KafkaConsumer<String, String> committersNumberMetricConsumer;

    private static KafkaConsumer<String, String> topCommittersMetricConsumer;

    private static KafkaConsumer<String, String> usedLanguagesMetricConsumer;

    private static AccountsAnalyzerApp accountsAnalyzerApp;

    private static List<MetricsKafkaStream> streams = new ArrayList<>();

    private static ObjectMapper objectMapper;

    private static GithubApiMockServer mockServer;

    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(3, true,
            "github-accounts",
            "github-commits",
            TOTAL_COMMITS_NUMBER_TOPIC,
            COMMITTERS_NUMBER_TOPIC,
            TOP_COMMITTERS_TOPIC,
            USED_LANGUAGES_TOPIC);

    @BeforeClass
    public static void setUp() throws JsonProcessingException {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        mockServer = new GithubApiMockServer(objectMapper);

        final String bootstrapServers = (String) KafkaTestUtils.consumerProps(
                "sender", "false", embeddedKafka.getEmbeddedKafka()).get("bootstrap.servers");
        final GithubMetricsBuilder githubMetricsBuilder = new GithubMetricsBuilder(bootstrapServers);

        accountsProducer = githubMetricsBuilder.createAccountsProducer();
        accountsAnalyzerApp = githubMetricsBuilder.createAccountsAnalyzer(mockServer.getMockServerUrl());
        streams = githubMetricsBuilder.createStreams();

        commitsConsumer = githubMetricsBuilder.createCommitsConsumer();
        totalCommitsMetricConsumer = githubMetricsBuilder.createTotalCommitsMetricConsumer();
        committersNumberMetricConsumer = githubMetricsBuilder.createCommittersNumberMetricConsumer();
        topCommittersMetricConsumer = githubMetricsBuilder.createTopCommittesMetricConsumer();
        usedLanguagesMetricConsumer = githubMetricsBuilder.createUsedLanguagesMetricConsumer();
    }

    @AfterClass
    public static void tearDown() {
        accountsProducer.close();
        accountsAnalyzerApp.close();
        streams.forEach(MetricsKafkaStream::close);
        streams.forEach(MetricsKafkaStream::cleanUp); // Clean up stores

        commitsConsumer.close();
        totalCommitsMetricConsumer.close();
        committersNumberMetricConsumer.close();
        topCommittersMetricConsumer.close();
        usedLanguagesMetricConsumer.close();

        embeddedKafka.getEmbeddedKafka().destroy();
        mockServer.stop();
    }

    @Test
    public void metricsAreMeasuredByGithubAccountMessage() throws IOException {
        // Prepare testing input and expectations
        Account account = new Account().setAccount("githubLogin").setInterval("5d");

        Commit expectedCommit1 = new Commit()
                .setAuthor(account.getAccount())
                .setLanguage("Java")
                .setMessage("Test commit message")
                .setRepositoryFullName("repository")
                .setSha("sha1")
                .setDateTimeUtc(LocalDateTime.now());

        Commit expectedCommit2 = new Commit()
                .setAuthor(account.getAccount())
                .setLanguage("Scala")
                .setMessage("Test commit message")
                .setRepositoryFullName("repository")
                .setSha("sha2")
                .setDateTimeUtc(LocalDateTime.now());

        mockServer.createExpectationForGithubCommitsSearchRequest(
                account.getAccount(), expectedCommit1, expectedCommit2);

        // Produce initial accounts message
        ProducerRecord<String, String> record = new ProducerRecord<>(
                "github-accounts", "ignored", objectMapper.writeValueAsString(account));
        accountsProducer.send(record);
        accountsProducer.flush();

        // Run accounts analyzer pipeline; here requests to Github are done
        accountsAnalyzerApp.runPipeline();

        // Wait for commits messages to appear in Kafka
        await().atMost(Duration.ofSeconds(10)).until(() -> !commitsConsumer.poll(Duration.ofMillis(1000)).isEmpty());

        // Wait for metrics messages and assert the correctness of their contents
        waitAndAssertTotalCommitsMetric();
        waitAndAssertCommittersNumberMetric();
        waitAndAssertTopFiveCommittersMetric();
        waitAndAssertUsedLanguagesMetric();
    }

    private void waitAndAssertTotalCommitsMetric() {
        List<ConsumerRecord<String, String>> result = new ArrayList<>();
        await().atMost(Duration.ofSeconds(10)).until(() -> {
            ConsumerRecords<String, String> records = totalCommitsMetricConsumer.poll(Duration.ofMillis(1000));
            records.forEach(result::add);
            return result.size() > 1;
        });

        assertThat(result).extracting(ConsumerRecord::value)
                .containsSequence("total_commits: 1", "total_commits: 2");
    }

    private void waitAndAssertCommittersNumberMetric() {
        List<ConsumerRecord<String, String>> result = new ArrayList<>();
        await().atMost(Duration.ofSeconds(10)).until(() -> {
            ConsumerRecords<String, String> records = committersNumberMetricConsumer.poll(Duration.ofMillis(1000));
            records.forEach(result::add);
            return !result.isEmpty();
        });

        assertThat(result).extracting(ConsumerRecord::value)
                .contains("total_committers: 1");
    }

    private void waitAndAssertTopFiveCommittersMetric() {
        List<ConsumerRecord<String, String>> result = new ArrayList<>();
        await().atMost(Duration.ofSeconds(10)).until(() -> {
            ConsumerRecords<String, String> records = topCommittersMetricConsumer.poll(Duration.ofMillis(1000));
            records.forEach(result::add);
            return result.size() > 1;
        });

        assertThat(result).extracting(ConsumerRecord::value)
                .containsSequence("top5_committers: githubLogin (1)", "top5_committers: githubLogin (2)");
    }

    private void waitAndAssertUsedLanguagesMetric() {
        List<ConsumerRecord<String, String>> result = new ArrayList<>();
        await().atMost(Duration.ofSeconds(10)).until(() -> {
            ConsumerRecords<String, String> records = usedLanguagesMetricConsumer.poll(Duration.ofMillis(1000));
            records.forEach(result::add);
            return result.size() > 1;
        });

        assertThat(result).extracting(ConsumerRecord::value)
                .contains("Java: 1")
                .contains("Scala: 1");
    }
}
