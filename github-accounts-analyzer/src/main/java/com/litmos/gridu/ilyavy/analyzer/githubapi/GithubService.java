package com.litmos.gridu.ilyavy.analyzer.githubapi;

import com.litmos.gridu.ilyavy.analyzer.model.Commit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GithubService {

    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);

    private static final String BASE_URL = "https://api.github.com/";

    private static final String GITHUB_API_ACCEPT_HEADER = "application/vnd.github.cloak-preview";

    public Flux<Commit> pollCommits(String githubLogin, LocalDateTime startingDateTime) {
        logger.info("Polling commits for " + githubLogin);

        String searchQuery = String.format("author:%s+author-date:>%s", githubLogin,
                startingDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        return WebClient.create(BASE_URL)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("search/commits")
                        .queryParam("q", searchQuery)
                        .queryParam("sort", "author-date")
                        .build())
                .accept(MediaType.valueOf(GITHUB_API_ACCEPT_HEADER))
                .exchange()
                .flatMap(r -> r.bodyToMono(SearchResponse.class))
                .flatMapMany(searchResult -> Flux.fromArray(searchResult.getItems()))
                .flatMap(item -> WebClient.create(item.getRepository().getLanguagesUrl())
                        .get()
                        .exchange()
                        .flatMap(r -> r.bodyToMono(new ParameterizedTypeReference<Map<String, Long>>(){}))
                        .map(languages -> {
                            if (languages.isEmpty()) {
                                return "Undefined";
                            }
                            List<Map.Entry<String, Long>> entries = new ArrayList<>(languages.entrySet());
                            entries.sort(Map.Entry.comparingByValue());
                            String primaryLanguage = entries.get(entries.size() - 1).getKey();
                            return primaryLanguage;
                        })
                        .map(item::setLanguage))
                .map(GithubService::searchResultItemToCommit);
    }

    private static Commit searchResultItemToCommit(SearchResponse.SearchResultItem item) {
        Commit commit = new Commit();
        commit.setSha(item.getSha());
        commit.setDateTimeUtc(item.getCommit().getAuthor().getDate().toLocalDateTime());
        commit.setMessage(item.getCommit().getMessage());
        commit.setAuthor(item.getAuthor().getLogin());
        commit.setRepositoryFullName(item.getRepository().getFullName());
        commit.setLanguage(item.getLanguage());

        return commit;
    }
}
