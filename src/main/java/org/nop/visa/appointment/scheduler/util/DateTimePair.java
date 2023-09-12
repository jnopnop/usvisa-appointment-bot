package org.nop.visa.appointment.scheduler.util;

public record DateTimePair(String date, String time) {
    public static DateTimePair of(String date, String time) {
        return new DateTimePair(date, time);
    }
}
