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

/** Main class of the application, launches all the streams. */
public class MetricsApp {

    private static final Logger logger = LoggerFactory.getLogger(MetricsApp.class);

    private static final String INPUT_TOPIC = "github-commits";

    private static final String TOTAL_COMMITS_NUMBER_TOPIC = "github-metrics-total-commits";

    private static final String COMMITTERS_NUMBER_TOPIC = "github-metrics-total-committers";

    private static final String TOP_COMMITTERS_TOPIC = "github-metrics-top-committers";

    private static final String USED_LANGUAGES_TOPIC = "github-metrics-languages";

    private String bootstrapServers;

    private final List<MetricsKafkaStream> streams = new ArrayList<>();

    public MetricsApp(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public void launchStreams() {
        MetricsKafkaStream totalCommitsCounter = new TotalCommitsCounter(
                baseProperties(bootstrapServers), INPUT_TOPIC, TOTAL_COMMITS_NUMBER_TOPIC);
        totalCommitsCounter.start();
        streams.add(totalCommitsCounter);
        logger.info("Total commits counter stream is launched");

        MetricsKafkaStream committersCounter = new CommittersCounter(
                baseProperties(bootstrapServers), INPUT_TOPIC, COMMITTERS_NUMBER_TOPIC);
        committersCounter.start();
        streams.add(committersCounter);
        logger.info("Committers counter stream is launched");

        MetricsKafkaStream topFiveCommitters = new TopFiveCommitters(
                baseProperties(bootstrapServers), INPUT_TOPIC, TOP_COMMITTERS_TOPIC);
        topFiveCommitters.start();
        streams.add(topFiveCommitters);
        logger.info("Top committers stream is launched");

        MetricsKafkaStream usedLanguageCounter = new UsedLanguageCounter(
                baseProperties(bootstrapServers), INPUT_TOPIC, USED_LANGUAGES_TOPIC);
        usedLanguageCounter.start();
        streams.add(usedLanguageCounter);
        logger.info("Used programming languages counter stream is launched");

        // shutdown hook to correctly close the streams application
        Runtime.getRuntime().addShutdownHook(new Thread(() -> streams.forEach(MetricsKafkaStream::close)));
    }

    private static Properties baseProperties(String bootstrapServers) {
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
        return properties;
    }

    public List<MetricsKafkaStream> getStreams() {
        return streams;
    }

    public static void main(String[] args) {
        MetricsApp app = new MetricsApp("localhost:9092,localhost:9095,localhost:9098");
        app.launchStreams();
    }
}
