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
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.litmos.gridu.ilyavy.analyzer.model.Commit;
import com.litmos.gridu.ilyavy.metrics.streams.transformer.DeduplicateByKeyTransformer;
import com.litmos.gridu.ilyavy.metrics.streams.transformer.TopFiveCommittersTransformer;

/**
 * Calculates top five of committers by the amount of distinct commits each one has.
 * Expexrs {@link Commit} messages on the input topic.
 * Produces string value with the top five committers, e.g. "top5_committers: user1 (100), user2 (90)".
 */
public class TopFiveCommitters extends MetricsKafkaStream {

    private static final Logger logger = LoggerFactory.getLogger(TopFiveCommitters.class);

    private static final String DEDUPLICATE_COMMITS_STORE = "top5-committers-distinct-commits";

    private static final String COMMITS_BY_AUTHOR_STORE = "CommitsByAuthor";

    private final String inputTopic;

    private final String outputTopic;

    private final Properties properties;

    /**
     * Constracts top five committers calculator.
     *
     * @param properties  properties which will be used for KafkaStreams
     * @param inputTopic  the name of the input topic
     * @param outputTopic the name of the output topic
     */
    public TopFiveCommitters(Properties properties, String inputTopic, String outputTopic) {
        this.inputTopic = inputTopic;
        this.outputTopic = outputTopic;

        this.properties = properties;
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "commits-metrics-top-committers");
    }

    @Override
    Topology createTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        StoreBuilder<KeyValueStore<String, String>> keyValueStoreBuilder =
                Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore(DEDUPLICATE_COMMITS_STORE),
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
                .transform(() -> new DeduplicateByKeyTransformer(DEDUPLICATE_COMMITS_STORE), DEDUPLICATE_COMMITS_STORE)
                .selectKey((key, value) -> value)
                .groupByKey()
                .count(Materialized.as(COMMITS_BY_AUTHOR_STORE))
                .toStream()
                .transform(() -> new TopFiveCommittersTransformer(COMMITS_BY_AUTHOR_STORE), COMMITS_BY_AUTHOR_STORE);

        totalCommitsNumber.to(outputTopic, Produced.with(Serdes.String(), Serdes.String()));

        return builder.build();
    }

    @Override
    public void start() {
        streams = new KafkaStreams(createTopology(), properties);
        super.start();
    }
}
