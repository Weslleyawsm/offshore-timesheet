package com.leley.timesheet.repo;

import com.leley.timesheet.domain.Collaborator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CollaboratorRepository {

    public long create(Connection c, String name, String email) {
        String sql = "INSERT INTO collaborators(name, email, active, created_at) VALUES(?,?,1,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, Instant.now().toString());
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
        String sql = "UPDATE collaborators SET active=? WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Collaborator> findById(Connection c, long id) {
        String sql = "SELECT id,name,email,active FROM collaborators WHERE id=?";
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

    public List<Collaborator> list(Connection c, boolean onlyActive) {
        String sql = onlyActive
                ? "SELECT id,name,email,active FROM collaborators WHERE active=1 ORDER BY name"
                : "SELECT id,name,email,active FROM collaborators ORDER BY name";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Collaborator> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Collaborator map(ResultSet rs) throws Exception {
        return new Collaborator(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getInt("active") == 1
        );
    }
}
