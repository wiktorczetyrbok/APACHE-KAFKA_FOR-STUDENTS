package com.litmos.gridu.ilyavy.metrics.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.litmos.gridu.ilyavy.analyzer.model.Commit;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

public class TopCommitters implements MetricsKafkaStream {

    private static final Logger logger = LoggerFactory.getLogger(TopCommitters.class);

    private static final String TRANSFORMER_STORE = "distinct-commits-tc";

    private static final String COMMITS_BY_AUTHOR_STORE = "CommitsByAuthor";

    private KafkaStreams streams;

    private final String inputTopic;

    private final String outputTopic;

    public TopCommitters(Properties properties, String inputTopic, String outputTopic) {
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;

        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "commits-metrics-top-committers");

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

        KStream<String, String> totalCommitsNumber = builder
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
                .selectKey((key, value) -> value)
                .groupByKey()
                .count(Materialized.as(COMMITS_BY_AUTHOR_STORE))
                .toStream()
                .transform(TopCommittersTransformer::new, COMMITS_BY_AUTHOR_STORE);

        totalCommitsNumber.to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));

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

    static class TopCommittersTransformer implements Transformer<String, Long, KeyValue<String, String>> {

        private KeyValueStore<String, ValueAndTimestamp> store;

        @Override
        public void init(ProcessorContext context) {
            this.store = (KeyValueStore) context.getStateStore(COMMITS_BY_AUTHOR_STORE);
        }

        @Override
        public KeyValue<String, String> transform(String key, Long value) {
            KeyValueIterator<String, ValueAndTimestamp> iterator = store.all();
            List<KeyValue<String, ValueAndTimestamp>> authors = new ArrayList<>();

            while (iterator.hasNext()) {
                authors.add(iterator.next());
            }

            authors.sort(Comparator.comparing(o ->
                    ((KeyValue<String, ValueAndTimestamp<Long>>) o).value.value()).reversed());

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 5 && i < authors.size(); i++) {
                sb.append(authors.get(i).key)
                        .append(" (").append(authors.get(i).value.value()).append(")");
                if (i != 4 && i != authors.size() - 1) {
                    sb.append(", ");
                }
            }
            logger.debug("Top committers found: " + sb.toString());

            return new KeyValue<>("top5-committers", "top5-committers: " + sb.toString());
        }

        @Override
        public void close() {
        }
    }

    // TODO: can be declared in one place
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
