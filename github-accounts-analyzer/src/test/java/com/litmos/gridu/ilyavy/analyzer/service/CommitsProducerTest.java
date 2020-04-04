package com.litmos.gridu.ilyavy.analyzer.service;

import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.litmos.gridu.ilyavy.analyzer.model.Commit;

import static org.mockito.Mockito.*;

class CommitsProducerTest {

    private KafkaProducer<String, String> kafkaProducer;

    private CommitsProducer commitsProducer;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        kafkaProducer = mock(KafkaProducer.class);
        commitsProducer = new CommitsProducer("localhost:9090", "topic");
        commitsProducer.producer = kafkaProducer;

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Test
    void send() throws JsonProcessingException {
        Commit expected = new Commit()
                .setAuthor("githubLogin")
                .setLanguage("Java")
                .setMessage("Test commit message")
                .setRepositoryFullName("repository")
                .setSha("sha")
                .setDateTimeUtc(LocalDateTime.now());
        String commitJson = objectMapper.writeValueAsString(expected);

        ProducerRecord<String, String> record = new ProducerRecord<>("topic", expected.getSha(), commitJson);
        commitsProducer.send(expected);

        verify(kafkaProducer, times(1)).send(record);
    }

    @Test
    void close() {
        commitsProducer.close();
        verify(kafkaProducer, times(1)).flush();
        verify(kafkaProducer, times(1)).close();
    }
}
