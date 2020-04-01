package com.litmos.gridu.ilyavy.analyzer.service;

import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IntervalDeserializer {

    private static Pattern DURATION_IN_TEXT_FORMAT = Pattern.compile("^([\\+\\-]?\\d+)([a-zA-Z]{1,2})$");

    private static final Map<String, ChronoUnit> UNITS;

    static {
        Map<String, ChronoUnit> units = new HashMap<>();
        units.put("h", ChronoUnit.HOURS);
        units.put("d", ChronoUnit.DAYS);
        units.put("w", ChronoUnit.WEEKS);
        UNITS = Collections.unmodifiableMap(units);
    }

    public LocalDateTime countStartingDateTime(String interval) {
        Matcher matcher = DURATION_IN_TEXT_FORMAT.matcher(interval);

        Assert.isTrue(matcher.matches(),
                "Interval is malformed, it should be in format <number><unit>, e.g. 3h, 1d, 5w");
        long amount = Long.parseLong(matcher.group(1));
        ChronoUnit unit = UNITS.get(matcher.group(2).toLowerCase());
        Assert.notNull(unit, "Unsupported unit, only hours (h), days (d) and weeks (w) are supported");

        LocalDateTime result = null;
        switch (unit) {
            case HOURS:
                result = LocalDateTime.now(ZoneOffset.UTC).minusHours(amount);
                break;
            case DAYS:
                result = LocalDateTime.now(ZoneOffset.UTC).minusDays(amount);
                break;
            case WEEKS:
                result = LocalDateTime.now(ZoneOffset.UTC).minusWeeks(amount);
                break;
        }

        return result;
    }
}
