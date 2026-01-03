package com.leley.timesheet.repo;

import com.leley.timesheet.domain.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ClientRepository {

    public long create(Connection c, String name) {
        String sql = "INSERT INTO clients(name, created_at) VALUES(?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, Instant.now().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
                throw new IllegalStateException("Sem id gerado");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Client> findById(Connection c, long id) {
        String sql = "SELECT id,name FROM clients WHERE id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Client(rs.getLong("id"), rs.getString("name")));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<Client> list(Connection c) {
        String sql = "SELECT id,name FROM clients ORDER BY name";
        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Client> out = new ArrayList<>();
            while (rs.next()) out.add(new Client(rs.getLong("id"), rs.getString("name")));
            return out;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
