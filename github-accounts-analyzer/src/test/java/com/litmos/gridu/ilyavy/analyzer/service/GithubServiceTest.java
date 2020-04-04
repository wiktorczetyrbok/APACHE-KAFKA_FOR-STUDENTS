package com.litmos.gridu.ilyavy.analyzer.service;

import com.litmos.gridu.ilyavy.analyzer.githubapi.GithubService;
import com.litmos.gridu.ilyavy.analyzer.model.Commit;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

class GithubServiceTest {

    @Test
    void pollCommits() {
        GithubService githubService = new GithubService();
        LocalDateTime startingFrom = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        //List<Commit> commits = githubService.pollCommits("ilyavy", startingFrom);
    }
}
