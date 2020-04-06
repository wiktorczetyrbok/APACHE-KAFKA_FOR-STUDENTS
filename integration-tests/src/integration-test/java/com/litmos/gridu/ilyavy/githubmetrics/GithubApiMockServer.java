package com.litmos.gridu.ilyavy.githubmetrics;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import com.litmos.gridu.ilyavy.analyzer.githubapi.SearchResponse;
import com.litmos.gridu.ilyavy.analyzer.model.Commit;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.matchers.Times.exactly;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class GithubMockServerBuilder {

    private String host = "127.0.0.1";

    private int port = 3333;

    private ObjectMapper objectMapper;

    GithubMockServerBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    String getMockServerUrl() {
        return String.format("http://%s:%d", host, port);
    }

    ClientAndServer createMockServer() throws JsonProcessingException {
        ClientAndServer mockServer = startClientAndServer(3333);
        return mockServer;
    }

    public void createExpectationForGithubCommitsSearchRequest(String author, Commit... commits)
            throws JsonProcessingException {

        SearchResponse.SearchResultItem[] items = new SearchResponse.SearchResultItem[commits.length];
        for (int i = 0; i < commits.length; i++) {
            Commit commit = commits[i];
            commit.setAuthor(author);
            items[i] = createResultItemAndLanguageExpectation(commit);
        }

        SearchResponse response = new SearchResponse()
                .setTotalCount(commits.length)
                .setItems(items);
        String responseJson = objectMapper.writeValueAsString(response);

        new MockServerClient(host, port)
                .when(request()
                        .withMethod("GET")
                        .withPath("/search/commits")
                        .withQueryStringParameter("q", String.format("author:%s.*", author)), exactly(1))
                .respond(response()
                        .withStatusCode(200)
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8"))
                        .withBody(responseJson)
                );
    }

    private SearchResponse.SearchResultItem createResultItemAndLanguageExpectation(Commit commit)
            throws JsonProcessingException {

        Map<String, Long> languages = new HashMap<>();
        languages.put("Not a language", 0L);
        languages.put(commit.getLanguage(), 10_000L);

        new MockServerClient(host, port)
                .when(request()
                        .withMethod("GET")
                        .withPath(String.format("/repo/languages/%s", commit.getLanguage())), exactly(1))
                .respond(response()
                        .withStatusCode(200)
                        .withHeader(new Header("Content-Type", "application/json; charset=utf-8"))
                        .withBody(objectMapper.writeValueAsString(languages)));

        return new SearchResponse.SearchResultItem()
                .setSha(commit.getSha())
                .setAuthor(new SearchResponse.AuthorInfo().setLogin(commit.getAuthor()))
                .setRepository(new SearchResponse.RepositoryInfo()
                        .setFullName(commit.getRepositoryFullName())
                        .setLanguagesUrl(String.format(
                                "http://%s:%d/repo/languages/%s", host, port, commit.getLanguage())))
                .setCommit(new SearchResponse.CommitInfo()
                        .setAuthor(new SearchResponse.CommitAuthorInfo()
                                .setDate(ZonedDateTime.of(commit.getDateTimeUtc(), ZoneOffset.UTC)))
                        .setMessage(commit.getMessage()));
    }
}
