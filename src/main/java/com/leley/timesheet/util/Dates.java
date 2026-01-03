package com.leley.timesheet.util;

import java.time.LocalDate;

public final class Dates {
    private Dates() {}

    public static String toDb(LocalDate d) {
        return d.toString();
    }

    public static LocalDate fromDb(String s) {
        return LocalDate.parse(s);
    }
}
