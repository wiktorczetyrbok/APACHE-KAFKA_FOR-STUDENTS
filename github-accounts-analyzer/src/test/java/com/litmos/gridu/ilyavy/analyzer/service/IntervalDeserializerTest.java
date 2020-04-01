package com.litmos.gridu.ilyavy.analyzer.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IntervalDeserializerTest {

    private static final long TIME_COMPARISON_PRECISION_SECONDS = 3L;

    IntervalDeserializer deser = new IntervalDeserializer();

    @Test
    void countStartingDateTimeHours() {
        LocalDateTime expected = LocalDateTime.now(ZoneOffset.UTC).minusHours(2);
        assertThat(ChronoUnit.SECONDS.between(expected, deser.countStartingDateTime("2h")))
                .isBetween(0L, TIME_COMPARISON_PRECISION_SECONDS);
    }

    @Test
    void countStartingDateTimeDays() {
        LocalDateTime expected = LocalDateTime.now(ZoneOffset.UTC).minusDays(3);
        assertThat(ChronoUnit.SECONDS.between(expected, deser.countStartingDateTime("3d")))
                .isBetween(0L, TIME_COMPARISON_PRECISION_SECONDS);
    }

    @Test
    void countStartingDateTimeMonths() {
        LocalDateTime expected = LocalDateTime.now(ZoneOffset.UTC).minusWeeks(1);
        assertThat(ChronoUnit.SECONDS.between(expected, deser.countStartingDateTime("1w")))
                .isBetween(0L, TIME_COMPARISON_PRECISION_SECONDS);
    }

    @Test
    void countStartingDateTimeLetterIsInUppercase() {
        LocalDateTime expected = LocalDateTime.now(ZoneOffset.UTC).minusWeeks(5);
        assertThat(ChronoUnit.SECONDS.between(expected, deser.countStartingDateTime("5W")))
                .isBetween(0L, TIME_COMPARISON_PRECISION_SECONDS);
    }

    @Test
    void countStartingDateTimeIncorrectFormatThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> deser.countStartingDateTime("1week"));
    }

    @Test
    void countStartingDateTimeUnsupportedCronoUnitThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> deser.countStartingDateTime("1m"));
    }
}
