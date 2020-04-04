package com.litmos.gridu.ilyavy.analyzer.service;

import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.litmos.gridu.ilyavy.analyzer.model.Commit;

/**
 * Wrapper around KafkaProducer, produces github commits objects.
 * {@link Commit}
 */
public class CommitsProducer {

    private static final Logger logger = LoggerFactory.getLogger(CommitsProducer.class);

    KafkaProducer<String, String> producer;

    private final String topic;

    private final ObjectMapper objectMapper;

    /**
     * Constructs CommitsProducer with the provided parameters.
     *
     * @param bootstrapServers kafka producer's bootstrap servers
     * @param topic            kafka topic
     */
    public CommitsProducer(String bootstrapServers, String topic) {
        this.topic = topic;

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");

        producer = new KafkaProducer<>(properties);
    }

    /**
     * Proxies `send` call to the underlying kafka producer.
     *
     * @param commit commit to send to kafka
     */
    public void send(Commit commit) {
        logger.info("Pushing commit into kafka: " + commit);

        String commitJson = null;
        try {
            commitJson = objectMapper.writeValueAsString(commit);
        } catch (Exception e) {
            logger.warn("Cannot write commit as json string", e);
        }

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, commit.getSha(), commitJson);
        producer.send(record);
    }

    /**
     * Flushes and closes KafkaProducer.
     */
    public void close() {
        producer.flush();
        producer.close();
    }
}
