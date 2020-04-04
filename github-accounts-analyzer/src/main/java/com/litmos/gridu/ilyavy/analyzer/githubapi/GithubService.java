package com.litmos.gridu.ilyavy.analyzer.githubapi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.litmos.gridu.ilyavy.analyzer.model.Commit;

/** Wrapper for Github REST API. */
public class GithubService {

    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);

    private static final String BASE_URL = "https://api.github.com/";

    private static final String GITHUB_API_ACCEPT_HEADER = "application/vnd.github.cloak-preview";

    WebClient.Builder webClientBuilder = WebClient.builder();

    /**
     * Polls the commits made by the `githubLogin` author starting from `startingDateTime`.
     *
     * @param githubLogin      author of the commits
     * @param startingDateTime starting creation date of the commits of interest
     * @return flux of commits
     */
    public Flux<Commit> pollCommits(String githubLogin, LocalDateTime startingDateTime) {
        logger.info("Polling commits for " + githubLogin);

        String searchQuery = String.format("author:%s+author-date:>%s", githubLogin,
                startingDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));

        return webClientBuilder.baseUrl(BASE_URL).build()
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
                .flatMap(item -> getCommitLanguage(item).map(item::setLanguage))
                .map(GithubService::searchResultItemToCommit);
    }

    /**
     * Returns the primary programming language used in the repository of commit's origin.
     *
     * @param item github's search result item, from where the info about the repository can be extracted
     * @return mono of string with the primary language
     */
    Mono<String> getCommitLanguage(SearchResponse.SearchResultItem item) {
        return webClientBuilder.baseUrl(item.getRepository().getLanguagesUrl()).build()
                .get()
                .exchange()
                .flatMap(r -> r.bodyToMono(new ParameterizedTypeReference<Map<String, Long>>() {
                }))
                .map(languages -> {
                    if (languages.isEmpty()) {
                        return "Undefined";
                    }
                    List<Map.Entry<String, Long>> entries = new ArrayList<>(languages.entrySet());
                    entries.sort(Map.Entry.comparingByValue());
                    String primaryLanguage = entries.get(entries.size() - 1).getKey();
                    return primaryLanguage;
                });
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
