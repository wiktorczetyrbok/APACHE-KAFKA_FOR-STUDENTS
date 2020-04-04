package com.litmos.gridu.ilyavy.analyzer.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Spliterators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import com.litmos.gridu.ilyavy.analyzer.model.Account;

import static org.mockito.Mockito.*;

class AccountsConsumerTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    private KafkaConsumer<String, String> kafkaConsumer;

    private AccountsConsumer accountsConsumer;

    @BeforeEach
    void setUp() {
        kafkaConsumer = mock(KafkaConsumer.class);
        accountsConsumer = new AccountsConsumer("localhost:9090", "groupId");
        accountsConsumer.consumer = kafkaConsumer;
    }

    @Test
    void subscribe() {
        accountsConsumer.subscribe("topic");
        verify(kafkaConsumer, times(1)).subscribe(Collections.singletonList("topic"));
    }

    @Test
    void poll() throws JsonProcessingException {
        Account expected = new Account().setAccount("githubLogin").setInterval("1d");
        String expectedJson = objectMapper.writeValueAsString(expected);

        ConsumerRecords<String, String> consumerRecords = prepareConsumerRecords(expectedJson);
        when(kafkaConsumer.poll(any())).thenReturn(consumerRecords);

        StepVerifier.create(accountsConsumer.poll(Duration.ofMillis(1000)))
                .expectSubscription()
                .expectNext(expected)
                .expectComplete()
                .verify();
    }

    @Test
    void pollIncorrectAccountFormat() throws JsonProcessingException {
        ConsumerRecords<String, String> consumerRecords = prepareConsumerRecords("");
        when(kafkaConsumer.poll(any())).thenReturn(consumerRecords);

        StepVerifier.create(accountsConsumer.poll(Duration.ofMillis(1000)))
                .expectSubscription()
                .expectNextCount(0)
                .expectComplete()
                .verify();
    }

    private ConsumerRecords<String, String> prepareConsumerRecords(String expectedJson) throws JsonProcessingException {
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 1, 10, "key", expectedJson);

        List<ConsumerRecord<String, String>> records = new ArrayList<>();
        records.add(record);

        ConsumerRecords<String, String> consumerRecords = mock(ConsumerRecords.class);
        when(consumerRecords.iterator()).thenReturn(records.iterator());
        when(consumerRecords.spliterator()).thenReturn(Spliterators.spliteratorUnknownSize(records.iterator(), 0));

        return consumerRecords;
    }

    @Test
    void close() {
        accountsConsumer.close();
        verify(kafkaConsumer, times(1)).close();
    }
}