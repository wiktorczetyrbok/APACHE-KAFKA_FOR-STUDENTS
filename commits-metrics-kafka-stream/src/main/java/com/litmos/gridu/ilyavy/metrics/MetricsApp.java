package com.litmos.gridu.ilyavy.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.litmos.gridu.ilyavy.metrics.streams.*;

public class MetricsApp {

    private static final Logger logger = LoggerFactory.getLogger(MetricsApp.class);

    private static final String BOOTSTRAP_SERVERS = "localhost:9092,localhost:9095,localhost:9098";

    private static final String INPUT_TOPIC = "github-commits";

    private static final String TOTAL_COMMITS_NUMBER_TOPIC = "github-metrics-total-commits";

    private static final String COMMITTERS_NUMBER_TOPIC = "github-metrics-total-committers";

    private static final String TOP_COMMITTERS_TOPIC = "github-metrics-top-committers";

    private static final String USED_LANGUAGES_TOPIC = "github-metrics-languages";

    private static final List<MetricsKafkaStream> streams = new ArrayList<>();

    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0"); // TODO

        streams.add(new TotalCommitsCounter(properties, INPUT_TOPIC, TOTAL_COMMITS_NUMBER_TOPIC));
        logger.info("Total commits counter stream is launched");

        streams.add(new CommittersCounter(properties, INPUT_TOPIC, COMMITTERS_NUMBER_TOPIC));
        logger.info("Committers counter stream is launched");

        streams.add(new TopFiveCommitters(properties, INPUT_TOPIC, TOP_COMMITTERS_TOPIC));
        logger.info("Top committers stream is launched");

        streams.add(new UsedLanguageCounter(properties, INPUT_TOPIC, USED_LANGUAGES_TOPIC));
        logger.info("Used programming languages counter stream is launched");

        streams.forEach(MetricsKafkaStream::start);

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(() -> streams.forEach(MetricsKafkaStream::close)));
    }
}
