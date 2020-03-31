package com.litmos.gridu.ilyavy.analyzer.model;

import java.time.LocalDateTime;

public class Commit {

    private String author;

    private LocalDateTime dateTime;

    private String language;

    private String sha;

    private String repositoryFullName;

    public String getAuthor() {

        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    public String getRepositoryFullName() {
        return repositoryFullName;
    }

    public void setRepositoryFullName(String repositoryFullName) {
        this.repositoryFullName = repositoryFullName;
    }
}
