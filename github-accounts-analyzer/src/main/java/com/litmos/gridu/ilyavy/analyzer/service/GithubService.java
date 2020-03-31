package com.litmos.gridu.ilyavy.analyzer.service;

import com.litmos.gridu.ilyavy.analyzer.model.Account;
import com.litmos.gridu.ilyavy.analyzer.model.Commit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

public class GithubService {

    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);

    public List<Commit> pollCommits(List<Account> accounts, LocalDateTime startingDateTime) {

        return null;
    }
}
