package com.litmos.gridu.ilyavy.analyzer.model;

public class Account {

    private String account;

    private String interval;

    public Account() { }

    public Account(String account, String interval) {
        this.account = account;
        this.interval = interval;
    }

    public String getAccount() {
        return account;
    }

    public String getInterval() {
        return interval;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }
}
