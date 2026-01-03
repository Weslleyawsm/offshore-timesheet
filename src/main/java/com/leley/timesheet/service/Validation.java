package com.leley.timesheet.service;

import java.math.BigDecimal;

public final class Validation {
    private Validation() {}

    public static String required(String v, String field) {
        if (v == null) throw new IllegalArgumentException(field + " é obrigatório");
        String t = v.trim();
        if (t.isEmpty()) throw new IllegalArgumentException(field + " é obrigatório");
        return t;
    }

    public static BigDecimal positive(BigDecimal v, String field) {
        if (v == null) throw new IllegalArgumentException(field + " é obrigatório");
        if (v.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException(field + " deve ser maior que zero");
        return v;
    }
}
