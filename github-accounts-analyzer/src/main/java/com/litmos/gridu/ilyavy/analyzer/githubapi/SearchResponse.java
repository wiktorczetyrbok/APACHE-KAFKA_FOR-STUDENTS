package com.litmos.gridu.ilyavy.analyzer.githubapi;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The object representation of Github API answer to /search/commits request.
 * {@see https://developer.github.com/v3/search/#search-commits}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class SearchResponse {

    @JsonProperty("total_count")
    private int totalCount;

    private SearchResultItem[] items;

    public int getTotalCount() {
        return totalCount;
    }

    public SearchResponse setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        return this;
    }

    public SearchResultItem[] getItems() {
        return items;
    }

    public SearchResponse setItems(SearchResultItem[] items) {
        this.items = items;
        return this;
    }

    public static class SearchResultItem {

        private String sha;

        private CommitInfo commit;

        private AuthorInfo author;

        private RepositoryInfo repository;

        /**
         * The field is not provided by the API answer directly but is deduced
         * and set manually from languages_url repository's field.
         */
        @JsonIgnore
        private String language;

        public String getSha() {
            return sha;
        }

        public SearchResultItem setSha(String sha) {
            this.sha = sha;
            return this;
        }

        public CommitInfo getCommit() {
            return commit;
        }

        public SearchResultItem setCommit(CommitInfo commit) {
            this.commit = commit;
            return this;
        }

        public AuthorInfo getAuthor() {
            return author;
        }

        public SearchResultItem setAuthor(AuthorInfo author) {
            this.author = author;
            return this;
        }

        public RepositoryInfo getRepository() {
            return repository;
        }

        public SearchResultItem setRepository(RepositoryInfo repository) {
            this.repository = repository;
            return this;
        }

        public String getLanguage() {
            return language;
        }

        public SearchResultItem setLanguage(String language) {
            this.language = language;
            return this;
        }
    }

    public static class CommitInfo {

        private String message;

        private CommitAuthorInfo author;

        public String getMessage() {
            return message;
        }

        public CommitInfo setMessage(String message) {
            this.message = message;
            return this;
        }

        public CommitAuthorInfo getAuthor() {
            return author;
        }

        public CommitInfo setAuthor(CommitAuthorInfo author) {
            this.author = author;
            return this;
        }
    }

    public static class CommitAuthorInfo {

        private ZonedDateTime date;

        public ZonedDateTime getDate() {
            return date;
        }

        public CommitAuthorInfo setDate(ZonedDateTime date) {
            this.date = date;
            return this;
        }
    }

    public static class AuthorInfo {

        private String login;

        public String getLogin() {
            return login;
        }

        public AuthorInfo setLogin(String login) {
            this.login = login;
            return this;
        }
    }

    public static class RepositoryInfo {

        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("languages_url")
        private String languagesUrl;

        public String getFullName() {
            return fullName;
        }

        public RepositoryInfo setFullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public String getLanguagesUrl() {
            return languagesUrl;
        }

        public RepositoryInfo setLanguagesUrl(String languagesUrl) {
            this.languagesUrl = languagesUrl;
            return this;
        }
    }
}
