package com.leley.timesheet.repo;

import com.leley.timesheet.domain.Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ProjectRepository {

    public long create(Connection c, long clientId, String name) {
        String sql = "INSERT INTO projects(client_id,name,active,created_at) VALUES(?,?,1,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, clientId);
            ps.setString(2, name);
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
        String sql = "UPDATE projects SET active=? WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Project> findById(Connection c, long id) {
        String sql = "SELECT id,client_id,name,active FROM projects WHERE id=?";
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

    public List<Project> list(Connection c, boolean onlyActive) {
        String sql = onlyActive
                ? "SELECT id,client_id,name,active FROM projects WHERE active=1 ORDER BY name"
                : "SELECT id,client_id,name,active FROM projects ORDER BY name";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Project> out = new ArrayList<>();
            while (rs.next()) out.add(map(rs));
            return out;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Project> listByClient(Connection c, long clientId, boolean onlyActive) {
        String sql = onlyActive
                ? "SELECT id,client_id,name,active FROM projects WHERE client_id=? AND active=1 ORDER BY name"
                : "SELECT id,client_id,name,active FROM projects WHERE client_id=? ORDER BY name";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Project> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Project map(ResultSet rs) throws Exception {
        return new Project(
                rs.getLong("id"),
                rs.getLong("client_id"),
                rs.getString("name"),
                rs.getInt("active") == 1
        );
    }
}
