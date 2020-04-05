package com.litmos.gridu.ilyavy.metrics.streams;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.streams.KafkaStreams;
import org.junit.jupiter.api.Test;

import com.litmos.gridu.ilyavy.analyzer.model.Commit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TopFiveCommittersTest extends BaseTestStreams {

    @Override
    MetricsKafkaStream createMetricsKafkaStream(Properties properties) {
        return new TopFiveCommitters(properties, INPUT_TOPIC_NAME, OUTPUT_TOPIC_NAME);
    }

    @Test
    void top5IsCounted() throws JsonProcessingException {
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

        Commit commit3 = new Commit()
                .setAuthor("anotherGithubLogin")
                .setLanguage("Java")
                .setMessage("Test commit message 3")
                .setRepositoryFullName("repository3")
                .setSha("sha3")
                .setDateTimeUtc(LocalDateTime.now());
        String commit3Json = objectMapper.writeValueAsString(commit3);

        inputTopic.pipeInput(commit1.getSha(), commit1Json);
        inputTopic.pipeInput(commit2.getSha(), commit2Json);
        inputTopic.pipeInput(commit3.getSha(), commit3Json);

        assertThat(outputTopic.readValue()).isEqualTo("top5_committers: githubLogin (1)");
        assertThat(outputTopic.readValue()).isEqualTo("top5_committers: githubLogin (2)");
        assertThat(outputTopic.readValue()).isEqualTo("top5_committers: githubLogin (2), anotherGithubLogin (1)");
        assertTrue(outputTopic.isEmpty());
    }

    @Test
    void duplicateCommitsAreCountedAsOne() throws JsonProcessingException {
        Commit commit = new Commit()
                .setAuthor("githubLogin")
                .setLanguage("Java")
                .setMessage("Test commit message 1")
                .setRepositoryFullName("repository")
                .setSha("sha1")
                .setDateTimeUtc(LocalDateTime.now());
        String commitJson = objectMapper.writeValueAsString(commit);

        inputTopic.pipeInput(commit.getSha(), commitJson);
        inputTopic.pipeInput(commit.getSha(), commitJson);

        assertThat(outputTopic.readValue()).isEqualTo("top5_committers: githubLogin (1)");
        assertTrue(outputTopic.isEmpty());
    }

    @Test
    void top5ShowsOnlyTopFive() throws JsonProcessingException {
        List<Commit> commits = new ArrayList<>();

        commits.add(new Commit().setAuthor("githubLogin1").setSha("sha1"));
        commits.add(new Commit().setAuthor("githubLogin2").setSha("sha2"));
        commits.add(new Commit().setAuthor("githubLogin3").setSha("sha3"));
        commits.add(new Commit().setAuthor("githubLogin4").setSha("sha4"));
        commits.add(new Commit().setAuthor("githubLogin5").setSha("sha5"));
        commits.add(new Commit().setAuthor("githubLogin6").setSha("sha6"));

        for (Commit commit : commits) {
            inputTopic.pipeInput(commit.getSha(), objectMapper.writeValueAsString(commit));
        }

        assertThat(outputTopic.readValue()).isEqualTo("top5_committers: githubLogin1 (1)");
        assertThat(outputTopic.readValue()).isEqualTo("top5_committers: githubLogin1 (1), githubLogin2 (1)");
        assertThat(outputTopic.readValue()).isEqualTo(
                "top5_committers: githubLogin1 (1), githubLogin2 (1), githubLogin3 (1)");
        assertThat(outputTopic.readValue()).isEqualTo(
                "top5_committers: githubLogin1 (1), githubLogin2 (1), githubLogin3 (1), githubLogin4 (1)");
        assertThat(outputTopic.readValue()).isEqualTo(
                "top5_committers: githubLogin1 (1), githubLogin2 (1), " +
                        "githubLogin3 (1), githubLogin4 (1), githubLogin5 (1)");
        assertThat(outputTopic.readValue()).isEqualTo(
                "top5_committers: githubLogin1 (1), githubLogin2 (1), " +
                        "githubLogin3 (1), githubLogin4 (1), githubLogin5 (1)");
        assertTrue(outputTopic.isEmpty());
    }

    @Test
    void close() {
        TopFiveCommitters topCommitters =
                new TopFiveCommitters(new Properties(), INPUT_TOPIC_NAME, OUTPUT_TOPIC_NAME);
        topCommitters.streams = mock(KafkaStreams.class);

        topCommitters.close();
        verify(topCommitters.streams, times(1)).close();
    }
}
