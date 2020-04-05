package com.litmos.gridu.ilyavy.metrics.streams;

import java.time.LocalDateTime;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.litmos.gridu.ilyavy.analyzer.model.Commit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CommittersCounterTest {

    private static final String INPUT_TOPIC_NAME = "input";

    private static final String OUTPUT_TOPIC_NAME = "output";

    private TopologyTestDriver testDriver;

    private TestInputTopic<String, String> inputTopic;

    private TestOutputTopic<String, String> outputTopic;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Properties properties = new Properties();
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "test:1234");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        CommittersCounter committersCounter = new CommittersCounter(properties, INPUT_TOPIC_NAME, OUTPUT_TOPIC_NAME);
        Topology topology = committersCounter.createTopology();

        testDriver = new TopologyTestDriver(topology, properties);
        inputTopic = testDriver.createInputTopic(INPUT_TOPIC_NAME, new StringSerializer(), new StringSerializer());
        outputTopic = testDriver.createOutputTopic(OUTPUT_TOPIC_NAME, new StringDeserializer(), new StringDeserializer());

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @AfterEach
    public void closeTestDriver() {
        testDriver.close();
    }

    @Test
    void commitsAreCounted() throws JsonProcessingException {
        Commit commit1 = new Commit()
                .setAuthor("githubLogin")
                .setLanguage("Java")
                .setMessage("Test commit message 1")
                .setRepositoryFullName("repository")
                .setSha("sha1")
                .setDateTimeUtc(LocalDateTime.now());
        String commit1Json = objectMapper.writeValueAsString(commit1);

        Commit commit2 = new Commit()
                .setAuthor("anotherGithubLogin")
                .setLanguage("Java")
                .setMessage("Test commit message 2")
                .setRepositoryFullName("repository")
                .setSha("sha2")
                .setDateTimeUtc(LocalDateTime.now());
        String commit2Json = objectMapper.writeValueAsString(commit2);

        inputTopic.pipeInput(commit1.getSha(), commit1Json);
        inputTopic.pipeInput(commit2.getSha(), commit2Json);

        assertThat(outputTopic.readValue()).isEqualTo("total_committers: 1");
        assertThat(outputTopic.readValue()).isEqualTo("total_committers: 2");
        assertTrue(outputTopic.isEmpty());
    }

    @Test
    void duplicateCommittersAreCountedAsOne() throws JsonProcessingException {
        Commit commit1 = new Commit()
                .setAuthor("githubLogin")
                .setLanguage("Java")
                .setMessage("Test commit message 1")
                .setRepositoryFullName("repository")
                .setSha("sha1")
                .setDateTimeUtc(LocalDateTime.now());
        String commit1Json = objectMapper.writeValueAsString(commit1);

        Commit commit2 = new Commit()
                .setAuthor("githubLogin")
                .setLanguage("Java")
                .setMessage("Test commit message 2")
                .setRepositoryFullName("repository")
                .setSha("sha2")
                .setDateTimeUtc(LocalDateTime.now());
        String commit2Json = objectMapper.writeValueAsString(commit2);

        inputTopic.pipeInput(commit1.getSha(), commit1Json);
        inputTopic.pipeInput(commit2.getSha(), commit2Json);

        assertThat(outputTopic.readValue()).isEqualTo("total_committers: 1");
        assertTrue(outputTopic.isEmpty());
    }

    @Test
    void close() {
        CommittersCounter committersCounter =
                new CommittersCounter(new Properties(), INPUT_TOPIC_NAME, OUTPUT_TOPIC_NAME);
        committersCounter.streams = mock(KafkaStreams.class);

        committersCounter.close();
        verify(committersCounter.streams, times(1)).close();
    }
}
