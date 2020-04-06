package com.litmos.gridu.ilyavy.githubmetrics;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import com.litmos.gridu.ilyavy.analyzer.AccountsAnalyzerApp;
import com.litmos.gridu.ilyavy.metrics.MetricsApp;
import com.litmos.gridu.ilyavy.metrics.streams.MetricsKafkaStream;

/** A helper class for building GithubMetrics' modules. */
public class GithubMetricsBuilder {

    private static final String TOTAL_COMMITS_NUMBER_TOPIC = "github-metrics-total-commits";

    private static final String COMMITTERS_NUMBER_TOPIC = "github-metrics-total-committers";

    private static final String TOP_COMMITTERS_TOPIC = "github-metrics-top-committers";

    private static final String USED_LANGUAGES_TOPIC = "github-metrics-languages";

    private final String bootstrapServers;

    public GithubMetricsBuilder(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    KafkaProducer<String, String> createAccountsProducer() {
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");

        return new KafkaProducer<>(properties);
    }


    Properties commonConsumerProperties() {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return properties;
    }

    KafkaConsumer<String, String> createCommitsConsumer() {
        Properties properties = commonConsumerProperties();
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "commits-consumer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singleton("github-commits"));

        return consumer;
    }

    KafkaConsumer<String, String> createTotalCommitsMetricConsumer() {
        Properties properties = commonConsumerProperties();
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "total-commits-consumer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singleton(TOTAL_COMMITS_NUMBER_TOPIC));

        return consumer;
    }

    KafkaConsumer<String, String> createCommittersNumberMetricConsumer() {
        Properties properties = commonConsumerProperties();
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "committers-number-consumer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singleton(COMMITTERS_NUMBER_TOPIC));

        return consumer;
    }

    KafkaConsumer<String, String> createTopCommittesMetricConsumer() {
        Properties properties = commonConsumerProperties();
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "top-committers-consumer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singleton(TOP_COMMITTERS_TOPIC));

        return consumer;
    }

    KafkaConsumer<String, String> createUsedLanguagesMetricConsumer() {
        Properties properties = commonConsumerProperties();
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "used-languages-consumer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singleton(USED_LANGUAGES_TOPIC));

        return consumer;
    }

    List<MetricsKafkaStream> createStreams() {
        MetricsApp metricsApp = new MetricsApp(bootstrapServers);
        metricsApp.launchStreams();
        return metricsApp.getStreams();
    }

    AccountsAnalyzerApp createAccountsAnalyzer(String githubApiBaseUrl) {
        return new AccountsAnalyzerApp(bootstrapServers, githubApiBaseUrl);
    }
}
