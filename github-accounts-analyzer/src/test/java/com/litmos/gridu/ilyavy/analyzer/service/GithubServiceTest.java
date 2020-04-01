package com.litmos.gridu.ilyavy.analyzer.service;

import com.litmos.gridu.ilyavy.analyzer.githubapi.GithubService;
import com.litmos.gridu.ilyavy.analyzer.model.Commit;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

class GithubServiceTest {

    @Test
    void pollCommits() {
        GithubService githubService = new GithubService();
        LocalDateTime startingFrom = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        List<Commit> commits = githubService.pollCommits("ilyavy", startingFrom);
    }
}