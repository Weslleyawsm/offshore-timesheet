package com.leley.timesheet.domain;

public record Project(Long id, Long clientId, String name, boolean active) {}
