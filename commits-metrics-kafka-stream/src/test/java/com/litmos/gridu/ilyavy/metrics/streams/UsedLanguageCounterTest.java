//package com.litmos.gridu.ilyavy.metrics.streams;
//
//import java.time.LocalDateTime;
//import java.util.Properties;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import org.apache.kafka.streams.KafkaStreams;
//import org.junit.jupiter.api.Test;
//
//import com.litmos.gridu.ilyavy.analyzer.model.Commit;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.mockito.Mockito.*;
//
//class UsedLanguageCounterTest extends BaseTestStreams {
//
//    @Override
//    MetricsKafkaStream createMetricsKafkaStream(Properties properties) {
//        return new UsedLanguageCounter(properties, INPUT_TOPIC_NAME, OUTPUT_TOPIC_NAME);
//    }
//
//    @Test
//    void languagesAreCounted() throws JsonProcessingException {
//        Commit commit1 = new Commit()
//                .setAuthor("githubLogin")
//                .setLanguage("Java")
//                .setMessage("Test commit message 1")
//                .setRepositoryFullName("repository")
//                .setSha("sha1")
//                .setDateTimeUtc(LocalDateTime.now());
//        String commit1Json = objectMapper.writeValueAsString(commit1);
//
//        Commit commit2 = new Commit()
//                .setAuthor("anotherGithubLogin")
//                .setLanguage("Scala")
//                .setMessage("Test commit message 2")
//                .setRepositoryFullName("repository")
//                .setSha("sha2")
//                .setDateTimeUtc(LocalDateTime.now());
//        String commit2Json = objectMapper.writeValueAsString(commit2);
//
//        Commit commit3 = new Commit()
//                .setAuthor("anotherGithubLogin")
//                .setLanguage("Java")
//                .setMessage("Test commit message 3")
//                .setRepositoryFullName("repository3")
//                .setSha("sha3")
//                .setDateTimeUtc(LocalDateTime.now());
//        String commit3Json = objectMapper.writeValueAsString(commit3);
//
//        inputTopic.pipeInput(commit1.getSha(), commit1Json);
//        inputTopic.pipeInput(commit2.getSha(), commit2Json);
//        inputTopic.pipeInput(commit3.getSha(), commit3Json);
//
//        assertThat(outputTopic.readValue()).isEqualTo("Java: 1");
//        assertThat(outputTopic.readValue()).isEqualTo("Scala: 1");
//        assertThat(outputTopic.readValue()).isEqualTo("Java: 2");
//        assertTrue(outputTopic.isEmpty());
//    }
//
//    @Test
//    void duplicateCommitsAreCountedAsOne() throws JsonProcessingException {
//        Commit commit = new Commit()
//                .setAuthor("githubLogin")
//                .setLanguage("Java")
//                .setMessage("Test commit message 1")
//                .setRepositoryFullName("repository")
//                .setSha("sha1")
//                .setDateTimeUtc(LocalDateTime.now());
//        String commitJson = objectMapper.writeValueAsString(commit);
//
//        inputTopic.pipeInput(commit.getSha(), commitJson);
//        inputTopic.pipeInput(commit.getSha(), commitJson);
//
//        assertThat(outputTopic.readValue()).isEqualTo("Java: 1");
//        assertTrue(outputTopic.isEmpty());
//    }
//
//    @Test
//    void close() {
//        UsedLanguageCounter usedLanguageCounter =
//                new UsedLanguageCounter(new Properties(), INPUT_TOPIC_NAME, OUTPUT_TOPIC_NAME);
//        usedLanguageCounter.streams = mock(KafkaStreams.class);
//
//        usedLanguageCounter.close();
//        verify(usedLanguageCounter.streams, times(1)).close();
//    }
//}
