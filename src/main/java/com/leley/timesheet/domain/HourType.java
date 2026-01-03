package com.leley.timesheet.domain;

import java.math.BigDecimal;

public record HourType(Long id, String code, String description, BigDecimal rateValue, String currency, boolean active) {}
