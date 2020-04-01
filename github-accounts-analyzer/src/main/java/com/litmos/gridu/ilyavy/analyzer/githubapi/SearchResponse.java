package com.litmos.gridu.ilyavy.analyzer.githubapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
class SearchResponse {

    @JsonProperty("total_count")
    private int totalCount;

    private SearchResultItem[] items;

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public SearchResultItem[] getItems() {
        return items;
    }

    public void setItems(SearchResultItem[] items) {
        this.items = items;
    }

    public static class SearchResultItem {

        private String sha;

        private CommitInfo commit;

        private RepositoryInfo repository;

        public String getSha() {
            return sha;
        }

        public void setSha(String sha) {
            this.sha = sha;
        }

        public CommitInfo getCommit() {
            return commit;
        }

        public void setCommit(CommitInfo commit) {
            this.commit = commit;
        }

        public RepositoryInfo getRepository() {
            return repository;
        }

        public void setRepository(RepositoryInfo repository) {
            this.repository = repository;
        }
    }

    public static class CommitInfo {

        private String message;

        private CommitAuthorInfo author;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public CommitAuthorInfo getAuthor() {
            return author;
        }

        public void setAuthor(CommitAuthorInfo author) {
            this.author = author;
        }
    }

    public static class CommitAuthorInfo {

        private ZonedDateTime date;

        public ZonedDateTime getDate() {
            return date;
        }

        public void setDate(ZonedDateTime date) {
            this.date = date;
        }
    }

    public static class RepositoryInfo {

        private String name;

        @JsonProperty("full_name")
        private String fullName;

        @JsonProperty("languages_url")
        private String languagesUrl;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getLanguagesUrl() {
            return languagesUrl;
        }

        public void setLanguagesUrl(String languagesUrl) {
            this.languagesUrl = languagesUrl;
        }
    }
}
