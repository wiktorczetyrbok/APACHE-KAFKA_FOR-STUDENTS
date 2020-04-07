package com.litmos.gridu.ilyavy.metrics.streams;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

/**
 * Interface for simple wrappers around {@link KafkaStreams}.
 */
public abstract class MetricsKafkaStream {

    KafkaStreams streams;

    /** Proxies the call to KafkaStreams `start` method. */
    public void start() {
        streams.start();
    }

    /** Proxies the call to KafkaStreams `close` method. */
    public void close() {
        streams.close();
    }

    /** Proxies the call to KafkaStreams `cleanUp` method. */
    public void cleanUp() {
        streams.cleanUp();
    }

    /** Creates the topology for KafkaStreams. */
    abstract Topology createTopology();
}
