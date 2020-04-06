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
 * Counts total number of distinct commits (by their hash).
 * Expexrs {@link Commit} messages on the input topic.
 * Produces string value with the number of commits, e.g. "total_commits: 5".
 */
public class TotalCommitsCounter extends MetricsKafkaStream {

    private static final Logger logger = LoggerFactory.getLogger(TotalCommitsCounter.class);

    private static final String TRANSFORMER_STORE = "total-commits-counter-distinct-commits";

    private final String inputTopic;

    private final String outputTopic;

    private final Properties properties;

    /**
     * Constracts total commits counter.
     *
     * @param properties  properties which will be used for KafkaStreams
     * @param inputTopic  the name of the input topic
     * @param outputTopic the name of the output topic
     */
    public TotalCommitsCounter(Properties properties, String inputTopic, String outputTopic) {
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;
        this.properties = properties;

        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "commits-metrics-total-commits-counter");
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

        KTable<String, String> totalCommitsNumber = builder
                .stream(inputTopic, Consumed.with(Serdes.String(), Serdes.String()))
                .transform(() -> new DeduplicateByKeyTransformer(TRANSFORMER_STORE), TRANSFORMER_STORE)
                .selectKey((key, value) -> {
                    return "total-commits-number";
                })
                .groupByKey()
                .count(Materialized.as("TotalCommitsCount"))
                .mapValues(value -> "total_commits: " + value);

        totalCommitsNumber.toStream().to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));

        return builder.build();
    }

    @Override
    public void start() {
        streams = new KafkaStreams(createTopology(), properties);
        super.start();
    }
}
