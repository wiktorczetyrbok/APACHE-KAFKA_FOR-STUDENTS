package com.litmos.gridu.ilyavy.analyzer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.litmos.gridu.ilyavy.analyzer.model.Account;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class AccountsConsumer {

    private static final Logger logger = LoggerFactory.getLogger(AccountsConsumer.class);

    private final KafkaConsumer<String, String> consumer;

    private final ObjectMapper objectMapper;

    public AccountsConsumer(String bootstrapServers, String topic, String groupId) {
        objectMapper = new ObjectMapper();
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(Collections.singletonList(topic));
    }

    public Flux<Account> poll(Duration timeout) {
        return Flux.fromIterable(consumer.poll(timeout))
                .doOnNext(r -> logger.info("Partition: " + r.partition() + ", Offset:" + r.offset()))
                .doOnNext(r -> logger.info("Key: " + r.key() + ", Value: " + r.value()))
                .map(ConsumerRecord::value)
                .flatMap(this::jsonStringToAccount);
    }

    private Mono<Account> jsonStringToAccount(String value) {
        try {
            return Mono.just(objectMapper.readValue(value, Account.class));
        } catch (JsonProcessingException e) {
            logger.warn("Cannot read the value - data may be malformed", e);
        }
        return Mono.empty();
    }

    public void close() {
        consumer.close();
    }
}
