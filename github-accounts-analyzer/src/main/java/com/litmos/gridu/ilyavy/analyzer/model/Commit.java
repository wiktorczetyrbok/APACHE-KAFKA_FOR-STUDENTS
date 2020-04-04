package com.litmos.gridu.ilyavy.analyzer.model;

import java.time.LocalDateTime;
import java.util.Objects;

/** The representation of commit record. */
public class Commit {

    /** Github login of the author of the commit. */
    private String author;

    /** Date/time when the commit was created, in ISO 8601 format with UTC timezone. */
    private LocalDateTime dateTimeUtc;

    /** Programming language of the commit. */
    private String language;

    /** Hash of the commit. */
    private String sha;

    /** Message of the commit. */
    private String message;

    /** The fullname of the commit's repository. */
    private String repositoryFullName;

    public String getAuthor() {

        return author;
    }

    public Commit setAuthor(String author) {
        this.author = author;
        return this;
    }

    public LocalDateTime getDateTimeUtc() {
        return dateTimeUtc;
    }

    public Commit setDateTimeUtc(LocalDateTime dateTimeUtc) {
        this.dateTimeUtc = dateTimeUtc;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public Commit setLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getSha() {
        return sha;
    }

    public Commit setSha(String sha) {
        this.sha = sha;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Commit setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getRepositoryFullName() {
        return repositoryFullName;
    }

    public Commit setRepositoryFullName(String repositoryFullName) {
        this.repositoryFullName = repositoryFullName;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commit commit = (Commit) o;
        return Objects.equals(author, commit.author) &&
                Objects.equals(dateTimeUtc, commit.dateTimeUtc) &&
                Objects.equals(language, commit.language) &&
                Objects.equals(sha, commit.sha) &&
                Objects.equals(message, commit.message) &&
                Objects.equals(repositoryFullName, commit.repositoryFullName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, dateTimeUtc, language, sha, message, repositoryFullName);
    }

    @Override
    public String toString() {
        return "Commit{" +
                "author='" + author + '\'' +
                ", dateTimeUtc=" + dateTimeUtc +
                ", language='" + language + '\'' +
                ", sha='" + sha + '\'' +
                ", message='" + message + '\'' +
                ", repositoryFullName='" + repositoryFullName + '\'' +
                '}';
    }
}
