package com.litmos.gridu.ilyavy.metrics.streams;

import org.apache.kafka.streams.KafkaStreams;

/**
 * Interface for simple wrappers around {@link KafkaStreams}.
 */
public interface MetricsKafkaStream {

    /** Proxies the call to KafkaStreams `start` method. */
    void start();

    /** Proxies the call to KafkaStreams `close` method. */
    void close();
}
