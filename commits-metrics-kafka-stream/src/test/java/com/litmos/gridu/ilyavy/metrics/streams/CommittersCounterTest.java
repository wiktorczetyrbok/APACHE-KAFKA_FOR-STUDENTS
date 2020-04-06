package com.litmos.gridu.ilyavy.metrics.streams;

import java.time.LocalDateTime;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.streams.KafkaStreams;
import org.junit.jupiter.api.Test;

import com.litmos.gridu.ilyavy.analyzer.model.Commit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class CommittersCounterTest extends BaseTestStreams {

    @Override
    MetricsKafkaStream createMetricsKafkaStream(Properties properties) {
        return new CommittersCounter(properties, INPUT_TOPIC_NAME, OUTPUT_TOPIC_NAME);
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
                .setLanguage("Scala")
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
