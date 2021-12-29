package com.ccsltd.twitter.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

    private static Logger log = LoggerFactory.getLogger(Utils.class);

    public static final int SLEEP_SECONDS = 60;

    public static void handleRateLimitBreach(int rateLimitCount, int sleptForSecondsTotal) {
        log.info("Rate limit count = {}, waiting {} seconds. total slept time = {}", rateLimitCount, SLEEP_SECONDS,
                sleptForSecondsTotal);

        sleepForSeconds(SLEEP_SECONDS);
    }

    public static void sleepForSeconds(int seconds) {
        sleepForMilliSeconds(seconds * 1000);
    }

    public static void sleepForMilliSeconds(int milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static LocalDateTime convertDateToLocalDateTime(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date convertLocalDateAndAdjustToDate(LocalDate dateToConvert, int daysOffset) {
        // Getting system timezone
        ZoneId systemTimeZone = ZoneId.systemDefault();

        // converting LocalDateTime to ZonedDateTime with the system timezone
        ZonedDateTime zonedDateTime = dateToConvert.atStartOfDay(systemTimeZone);

        // converting ZonedDateTime to Date using Date.from() and ZonedDateTime.toInstant()
        return Date.from(zonedDateTime.minusDays(daysOffset).toInstant());
    }

}
