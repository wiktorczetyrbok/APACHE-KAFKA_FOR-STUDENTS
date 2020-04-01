package com.litmos.gridu.ilyavy.analyzer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.litmos.gridu.ilyavy.analyzer.model.Commit;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class CommitsProducer {

    private static final Logger logger = LoggerFactory.getLogger(CommitsProducer.class);

    private final KafkaProducer<String, String> producer;

    private final String topic;

    private final ObjectMapper objectMapper;

    public CommitsProducer(String bootstrapServers, String topic) {
        this.topic = topic;

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        producer = new KafkaProducer<>(properties);
    }

    public void push(Commit commit) {
        String commitJson = null;
        try {
            commitJson = objectMapper.writeValueAsString(commit);
        } catch (JsonProcessingException e) {
            logger.warn("Cannot read the value - data may be malformed", e);
        }

        ProducerRecord<String, String> record = new ProducerRecord<>(topic, commitJson);
        producer.send(record);
    }

    public void close() {
        producer.flush();
        producer.close();
    }
}
