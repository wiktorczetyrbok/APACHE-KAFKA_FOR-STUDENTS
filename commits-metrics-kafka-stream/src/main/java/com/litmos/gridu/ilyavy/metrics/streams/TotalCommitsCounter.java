package com.litmos.gridu.ilyavy.metrics.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.litmos.gridu.ilyavy.analyzer.model.Commit;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class TotalCommitsCounter implements MetricsKafkaStream {

    private static final Logger logger = LoggerFactory.getLogger(TotalCommitsCounter.class);

    private static final String TRANSFORMER_STORE = "distinct-commits";

    private KafkaStreams streams;

    private final String inputTopic;

    private final String outputTopic;

    public TotalCommitsCounter(Properties properties, String inputTopic, String outputTopic) {
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;

        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "commits-metrics-total-commits-counter");

        streams = new KafkaStreams(createTopology(), properties);
    }

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
                .mapValues((key, value) -> {
                    Commit commit = null;
                    try {
                        commit = objectMapper.readValue(value, Commit.class);
                    } catch (Exception e) {
                        logger.warn("Cannot read the value - data may be malformed", e);
                    }
                    return commit != null ? commit.getAuthor() : null;
                })
                .transform(DistinctCommitsTransformer::new, TRANSFORMER_STORE)
                .selectKey((key, value) -> "total-commits-number")
                .groupByKey()
                .count(Materialized.as("TotalCommitsCounts"))
                .mapValues(value -> "total-commits-number   " + value);

        totalCommitsNumber.toStream().to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));

        return builder.build();
    }

    @Override
    public void start() {
        streams.start();
    }

    @Override
    public void close() {
        streams.close();
    }

    static class DistinctCommitsTransformer implements Transformer<String, String, KeyValue<String, String>> {

        private KeyValueStore<String, String> store;

        @Override
        public void init(ProcessorContext context) {
            this.store = (KeyValueStore) context.getStateStore(TRANSFORMER_STORE);
        }

        @Override
        public KeyValue<String, String> transform(String key, String value) {
            String res = store.putIfAbsent(key, value);
            System.out.println("lookup result for key " + key + ": " + res);
            if (res != null) {
                return null;
            }
            return new KeyValue<>(key, value);
        }

        @Override
        public void close() {
        }
    }
}
