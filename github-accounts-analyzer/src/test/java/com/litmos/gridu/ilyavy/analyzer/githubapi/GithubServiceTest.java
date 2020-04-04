package com.litmos.gridu.ilyavy.analyzer.githubapi;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import com.litmos.gridu.ilyavy.analyzer.model.Commit;

class GithubServiceTest {

    @Test
    void pollCommits() throws JsonProcessingException {
        Commit expected = new Commit()
                .setAuthor("githubLogin")
                .setLanguage("Java")
                .setMessage("Test commit message")
                .setRepositoryFullName("repository")
                .setSha("sha")
                .setDateTimeUtc(LocalDateTime.now());

        ClientResponse commitsResponse = preparePollCommitsGithubResponse(expected);

        GithubServiceMockedLanguageRequest service = new GithubServiceMockedLanguageRequest();
        service.webClientBuilder = WebClient.builder().exchangeFunction(request -> Mono.just(commitsResponse));

        StepVerifier.create(service.pollCommits("test", LocalDateTime.now()))
                .expectSubscription()
                .expectNext(expected)
                .expectComplete()
                .verify();
    }

    private static ClientResponse preparePollCommitsGithubResponse(Commit expected) throws JsonProcessingException {
        SearchResponse.SearchResultItem item = new SearchResponse.SearchResultItem()
                .setSha(expected.getSha())
                .setAuthor(new SearchResponse.AuthorInfo().setLogin(expected.getAuthor()))
                .setRepository(new SearchResponse.RepositoryInfo()
                        .setFullName(expected.getRepositoryFullName())
                        .setLanguagesUrl("https://github/languages"))
                .setCommit(new SearchResponse.CommitInfo()
                        .setAuthor(new SearchResponse.CommitAuthorInfo()
                                .setDate(ZonedDateTime.of(expected.getDateTimeUtc(), ZoneOffset.UTC)))
                        .setMessage(expected.getMessage()));

        SearchResponse response = new SearchResponse()
                .setTotalCount(1)
                .setItems(new SearchResponse.SearchResultItem[]{item});

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        String responseJson = objectMapper.writeValueAsString(response);

        return ClientResponse
                .create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(responseJson).build();
    }

    @Test
    void pollCommitsGithubError() throws JsonProcessingException {
        ClientResponse commitsResponse = ClientResponse
                .create(HttpStatus.SERVICE_UNAVAILABLE)
                .body("").build();

        GithubServiceMockedLanguageRequest service = new GithubServiceMockedLanguageRequest();
        service.webClientBuilder = WebClient.builder().exchangeFunction(request -> Mono.just(commitsResponse));

        StepVerifier.create(service.pollCommits("test", LocalDateTime.now()))
                .expectSubscription()
                .expectNextCount(0)
                .expectError()
                .verify();
    }

    @Test
    void getCommitLanguage() throws JsonProcessingException {
        final String primaryLanguage = "Java";
        ClientResponse response = prepareGetCommitLanguageGithubResponse(primaryLanguage);

        SearchResponse.SearchResultItem item = new SearchResponse.SearchResultItem()
                .setRepository(new SearchResponse.RepositoryInfo().setLanguagesUrl("https://github.com/languages"));

        GithubService service = new GithubService();
        service.webClientBuilder = WebClient.builder().exchangeFunction(request -> Mono.just(response));

        StepVerifier.create(service.getCommitLanguage(item))
                .expectSubscription()
                .expectNext(primaryLanguage)
                .expectComplete()
                .verify();
    }

    private static ClientResponse prepareGetCommitLanguageGithubResponse(String primaryLanguage)
            throws JsonProcessingException {

        Map<String, Long> languagesResponse = new HashMap<>();
        languagesResponse.put(primaryLanguage, 10_000L);
        languagesResponse.put("Scala", 2000L);
        languagesResponse.put("Kotlin", 3000L);

        ObjectMapper objectMapper = new ObjectMapper();
        String responseJson = objectMapper.writeValueAsString(languagesResponse);

        return ClientResponse
                .create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(responseJson).build();
    }

    static class GithubServiceMockedLanguageRequest extends GithubService {
        @Override
        Mono<String> getCommitLanguage(SearchResponse.SearchResultItem item) {
            return Mono.just("Java");
        }
    }
}
