package com.leley.timesheet.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TimeEntry(Long id, Long collaboratorId, Long projectId, Long hourTypeId, LocalDate workDate, BigDecimal hours, String notes) {}
