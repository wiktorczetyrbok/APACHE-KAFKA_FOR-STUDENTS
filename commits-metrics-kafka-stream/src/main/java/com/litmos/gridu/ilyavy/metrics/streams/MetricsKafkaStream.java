package com.litmos.gridu.ilyavy.metrics.streams;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;

/**
 * Interface for simple wrappers around {@link KafkaStreams}.
 */
public abstract class MetricsKafkaStream {

    /** Proxies the call to KafkaStreams `start` method. */
    abstract public void start();

    /** Proxies the call to KafkaStreams `close` method. */
    abstract public void close();

    /** Creates the topology for KafkaStreams. */
    abstract Topology createTopology();
}
