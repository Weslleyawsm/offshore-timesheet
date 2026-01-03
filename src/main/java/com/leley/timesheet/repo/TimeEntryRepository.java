package com.leley.timesheet.repo;

import com.leley.timesheet.domain.TimeEntry;
import com.leley.timesheet.util.Dates;
import com.leley.timesheet.util.Money;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TimeEntryRepository {

    public long create(Connection c, long collaboratorId, long projectId, long hourTypeId, LocalDate workDate, BigDecimal hours, String notes) {
        String sql = "INSERT INTO time_entries(collaborator_id,project_id,hour_type_id,work_date,hours,notes,created_at) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, collaboratorId);
            ps.setLong(2, projectId);
            ps.setLong(3, hourTypeId);
            ps.setString(4, Dates.toDb(workDate));
            ps.setString(5, Money.toDb(hours));
            ps.setString(6, notes);
            ps.setString(7, Instant.now().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                throw new IllegalStateException("Sem id gerado");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(Connection c, long id) {
        String sql = "DELETE FROM time_entries WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<TimeEntry> findById(Connection c, long id) {
        String sql = "SELECT id,collaborator_id,project_id,hour_type_id,work_date,hours,notes FROM time_entries WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(map(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<TimeEntry> listByDateRange(Connection c, LocalDate from, LocalDate to) {
        String sql = """
                SELECT id,collaborator_id,project_id,hour_type_id,work_date,hours,notes
                FROM time_entries
                WHERE work_date >= ? AND work_date <= ?
                ORDER BY work_date ASC, id ASC
                """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Dates.toDb(from));
            ps.setString(2, Dates.toDb(to));
            try (ResultSet rs = ps.executeQuery()) {
                List<TimeEntry> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private TimeEntry map(ResultSet rs) throws Exception {
        return new TimeEntry(
                rs.getLong("id"),
                rs.getLong("collaborator_id"),
                rs.getLong("project_id"),
                rs.getLong("hour_type_id"),
                Dates.fromDb(rs.getString("work_date")),
                Money.fromDb(rs.getString("hours")),
                rs.getString("notes")
        );
    }
}
