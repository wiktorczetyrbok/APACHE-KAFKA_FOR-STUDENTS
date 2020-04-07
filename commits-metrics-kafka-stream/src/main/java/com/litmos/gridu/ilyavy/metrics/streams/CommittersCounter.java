package com.litmos.gridu.ilyavy.metrics.streams;

import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.litmos.gridu.ilyavy.analyzer.model.Commit;
import com.litmos.gridu.ilyavy.metrics.streams.transformer.DeduplicateByKeyTransformer;

/**
 * Counts total number of distinct committers.
 * Expexrs {@link Commit} messages on the input topic.
 * Produces string value with the number of committers, e.g. "total_committers: 5".
 */
public class CommittersCounter extends MetricsKafkaStream {

    private static final Logger logger = LoggerFactory.getLogger(CommittersCounter.class);

    private static final String TRANSFORMER_STORE = "committers-counter-distinct-committers";

    private final String inputTopic;

    private final String outputTopic;

    private final Properties properties;

    /**
     * Constracts total committers counter.
     *
     * @param properties  properties which will be used for KafkaStreams
     * @param inputTopic  the name of the input topic
     * @param outputTopic the name of the output topic
     */
    public CommittersCounter(Properties properties, String inputTopic, String outputTopic) {
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
        this.properties = properties;

        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "commits-metrics-committers-counter");
    }

    @Override
    Topology createTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        StoreBuilder<KeyValueStore<String, String>> keyValueStoreBuilder =
                Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(TRANSFORMER_STORE),
                        Serdes.String(), Serdes.String());
        builder.addStateStore(keyValueStoreBuilder);

        KTable<String, String> committersCount = builder
                .stream(inputTopic, Consumed.with(Serdes.String(), Serdes.String()))
                .mapValues((key, value) -> {
                    Commit commit = null;
                    try {
                        commit = objectMapper.readValue(value, Commit.class);
                    } catch (Exception e) {
                        logger.warn("Cannot read the value - data may be malformed", e);
                    }
                    return commit != null ? commit.getAuthor() : null;
                })
                .selectKey((key, value) -> value)
                .transform(() -> new DeduplicateByKeyTransformer(TRANSFORMER_STORE), TRANSFORMER_STORE)
                .selectKey((key, value) -> "total_committers")
                .groupByKey()
                .count(Materialized.as("CommittersCount"))
                .mapValues(value -> "total_committers: " + value);

        committersCount.toStream().to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));

        return builder.build();
    }

    @Override
    public void start() {
        streams = new KafkaStreams(createTopology(), properties);
        super.start();
    }
}
