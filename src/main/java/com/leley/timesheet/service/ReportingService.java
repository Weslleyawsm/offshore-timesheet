package com.leley.timesheet.service;

import com.leley.timesheet.db.Database;
import com.leley.timesheet.db.Tx;
import com.leley.timesheet.util.Dates;
import com.leley.timesheet.util.Money;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class ReportingService {
    private final Database db;

    public ReportingService(Database db) {
        this.db = db;
    }

    public record CollaboratorSummary(long collaboratorId, String collaboratorName, BigDecimal totalHours, BigDecimal totalValue) {}
    public record HourTypeSummary(long hourTypeId, String code, String description, BigDecimal totalHours, BigDecimal totalValue, String currency) {}
    public record DetailedRow(LocalDate date, String collaborator, String client, String project, String hourTypeCode, BigDecimal hours, BigDecimal rate, BigDecimal value, String currency, String notes) {}

    public List<CollaboratorSummary> summaryByCollaborator(LocalDate from, LocalDate to) {
        return Tx.inTx(db, c -> {
            String sql = """
                    SELECT co.id AS collaborator_id, co.name AS collaborator_name,
                           SUM(CAST(te.hours AS REAL)) AS total_hours,
                           SUM(CAST(te.hours AS REAL) * CAST(ht.rate_value AS REAL)) AS total_value
                    FROM time_entries te
                    JOIN collaborators co ON co.id = te.collaborator_id
                    JOIN hour_types ht ON ht.id = te.hour_type_id
                    WHERE te.work_date >= ? AND te.work_date <= ?
                    GROUP BY co.id, co.name
                    ORDER BY total_value DESC, co.name ASC
                    """;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, Dates.toDb(from));
                ps.setString(2, Dates.toDb(to));
                try (ResultSet rs = ps.executeQuery()) {
                    List<CollaboratorSummary> out = new ArrayList<>();
                    while (rs.next()) {
                        out.add(new CollaboratorSummary(
                                rs.getLong("collaborator_id"),
                                rs.getString("collaborator_name"),
                                BigDecimal.valueOf(rs.getDouble("total_hours")),
                                BigDecimal.valueOf(rs.getDouble("total_value"))
                        ));
                    }
                    return out;
                }
            }
        });
    }

    public List<HourTypeSummary> summaryByHourType(LocalDate from, LocalDate to) {
        return Tx.inTx(db, c -> {
            String sql = """
                    SELECT ht.id AS hour_type_id, ht.code, ht.description, ht.currency,
                           SUM(CAST(te.hours AS REAL)) AS total_hours,
                           SUM(CAST(te.hours AS REAL) * CAST(ht.rate_value AS REAL)) AS total_value
                    FROM time_entries te
                    JOIN hour_types ht ON ht.id = te.hour_type_id
                    WHERE te.work_date >= ? AND te.work_date <= ?
                    GROUP BY ht.id, ht.code, ht.description, ht.currency
                    ORDER BY total_value DESC, ht.code ASC
                    """;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, Dates.toDb(from));
                ps.setString(2, Dates.toDb(to));
                try (ResultSet rs = ps.executeQuery()) {
                    List<HourTypeSummary> out = new ArrayList<>();
                    while (rs.next()) {
                        out.add(new HourTypeSummary(
                                rs.getLong("hour_type_id"),
                                rs.getString("code"),
                                rs.getString("description"),
                                BigDecimal.valueOf(rs.getDouble("total_hours")),
                                BigDecimal.valueOf(rs.getDouble("total_value")),
                                rs.getString("currency")
                        ));
                    }
                    return out;
                }
            }
        });
    }

    public BigDecimal totalValue(LocalDate from, LocalDate to) {
        return Tx.inTx(db, c -> {
            String sql = """
                    SELECT COALESCE(SUM(CAST(te.hours AS REAL) * CAST(ht.rate_value AS REAL)),0) AS total_value
                    FROM time_entries te
                    JOIN hour_types ht ON ht.id = te.hour_type_id
                    WHERE te.work_date >= ? AND te.work_date <= ?
                    """;
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, Dates.toDb(from));
                ps.setString(2, Dates.toDb(to));
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return BigDecimal.valueOf(rs.getDouble("total_value"));
                }
            }
        });
    }

    public List<DetailedRow> detailed(LocalDate from, LocalDate to, Long collaboratorId, Long projectId, Long hourTypeId) {
        return Tx.inTx(db, c -> fetchDetailed(c, from, to, collaboratorId, projectId, hourTypeId));
    }

    private List<DetailedRow> fetchDetailed(Connection c, LocalDate from, LocalDate to, Long collaboratorId, Long projectId, Long hourTypeId) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                SELECT te.work_date, te.notes,
                       co.name AS collaborator,
                       cl.name AS client,
                       pr.name AS project,
                       ht.code AS hour_type_code,
                       te.hours,
                       ht.rate_value,
                       (CAST(te.hours AS REAL) * CAST(ht.rate_value AS REAL)) AS value,
                       ht.currency
                FROM time_entries te
                JOIN collaborators co ON co.id = te.collaborator_id
                JOIN projects pr ON pr.id = te.project_id
                JOIN clients cl ON cl.id = pr.client_id
                JOIN hour_types ht ON ht.id = te.hour_type_id
                WHERE te.work_date >= ? AND te.work_date <= ?
                """);

        List<Object> params = new ArrayList<>();
        params.add(Dates.toDb(from));
        params.add(Dates.toDb(to));

        if (collaboratorId != null) { sb.append(" AND te.collaborator_id = ?"); params.add(collaboratorId); }
        if (projectId != null) { sb.append(" AND te.project_id = ?"); params.add(projectId); }
        if (hourTypeId != null) { sb.append(" AND te.hour_type_id = ?"); params.add(hourTypeId); }

        sb.append(" ORDER BY te.work_date ASC, co.name ASC, cl.name ASC, pr.name ASC, ht.code ASC, te.id ASC");

        try (PreparedStatement ps = c.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object v = params.get(i);
                if (v instanceof String s) ps.setString(i + 1, s);
                else if (v instanceof Long l) ps.setLong(i + 1, l);
                else ps.setObject(i + 1, v);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<DetailedRow> out = new ArrayList<>();
                while (rs.next()) {
                    BigDecimal hours = Money.fromDb(rs.getString("hours"));
                    BigDecimal rate = Money.fromDb(rs.getString("rate_value"));
                    BigDecimal value = BigDecimal.valueOf(rs.getDouble("value"));
                    out.add(new DetailedRow(
                            Dates.fromDb(rs.getString("work_date")),
                            rs.getString("collaborator"),
                            rs.getString("client"),
                            rs.getString("project"),
                            rs.getString("hour_type_code"),
                            hours,
                            rate,
                            value,
                            rs.getString("currency"),
                            rs.getString("notes")
                    ));
                }
                return out;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
