package com.leley.timesheet.repo;

import com.leley.timesheet.domain.HourType;
import com.leley.timesheet.util.Money;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class HourTypeRepository {

    public long create(Connection c, String code, String description, BigDecimal rateValue, String currency) {
        String sql = "INSERT INTO hour_types(code,description,rate_value,currency,active,created_at) VALUES(?,?,?,?,1,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, description);
            ps.setString(3, Money.toDb(rateValue));
            ps.setString(4, currency);
            ps.setString(5, Instant.now().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                throw new IllegalStateException("Sem id gerado");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setActive(Connection c, long id, boolean active) {
        String sql = "UPDATE hour_types SET active=? WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<HourType> findById(Connection c, long id) {
        String sql = "SELECT id,code,description,rate_value,currency,active FROM hour_types WHERE id=?";
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

    public List<HourType> list(Connection c, boolean onlyActive) {
        String sql = onlyActive
                ? "SELECT id,code,description,rate_value,currency,active FROM hour_types WHERE active=1 ORDER BY code"
                : "SELECT id,code,description,rate_value,currency,active FROM hour_types ORDER BY code";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<HourType> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HourType map(ResultSet rs) throws Exception {
        return new HourType(
                rs.getLong("id"),
                rs.getString("code"),
                rs.getString("description"),
                Money.fromDb(rs.getString("rate_value")),
                rs.getString("currency"),
                rs.getInt("active") == 1
        );
    }
}
