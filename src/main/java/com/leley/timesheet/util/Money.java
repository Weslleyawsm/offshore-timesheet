package com.leley.timesheet.util;

import java.math.BigDecimal;

public final class Money {
    private Money() {}

    public static String toDb(BigDecimal v) {
        return v.stripTrailingZeros().toPlainString();
    }

    public static BigDecimal fromDb(String s) {
        if (s == null || s.isBlank()) return BigDecimal.ZERO;
        return new BigDecimal(s.trim());
    }
}
