package com.litmos.gridu.ilyavy.analyzer.model;

import java.util.Objects;

/** The representation of account record read from the file. */
public class Account {

    /** Github login. */
    private String account;

    /**
     * Interval, for which the commits should be polled.
     * Can be hours, days or weeks, and should have a format like "1d", "2w", "3h".
     */
    private String interval;

    public Account() {
    }

    public String getAccount() {
        return account;
    }

    public Account setAccount(String account) {
        this.account = account;
        return this;
    }

    public String getInterval() {
        return interval;
    }

    public Account setInterval(String interval) {
        this.interval = interval;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account1 = (Account) o;
        return Objects.equals(account, account1.account) &&
                Objects.equals(interval, account1.interval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, interval);
    }

    @Override
    public String toString() {
        return "Account{" +
                "account='" + account + '\'' +
                ", interval='" + interval + '\'' +
                '}';
    }
}
