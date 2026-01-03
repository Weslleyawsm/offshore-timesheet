package com.leley.timesheet.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Objects;
import java.util.Properties;

public final class Database {
    private final String url;
    private final boolean foreignKeys;

    public Database(String url, boolean foreignKeys) {
        this.url = Objects.requireNonNull(url);
        this.foreignKeys = foreignKeys;
    }

    public static Database fromClasspathProperties(String path) {
        try (InputStream in = Database.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalStateException("Não foi possível ler " + path);
            Properties p = new Properties();
            p.load(in);

            String url = p.getProperty("db.url");
            boolean fk = Boolean.parseBoolean(p.getProperty("db.foreign_keys", "true"));
            return new Database(url, fk);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Connection open() {
        try {
            Connection c = DriverManager.getConnection(url);
            if (foreignKeys) {
                try (Statement st = c.createStatement()) {
                    st.execute("PRAGMA foreign_keys = ON");
                }
            }
            return c;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void initSchema() {
        String sql = """
                CREATE TABLE IF NOT EXISTS clients(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  name TEXT NOT NULL UNIQUE,
                  created_at TEXT NOT NULL
                );

                CREATE TABLE IF NOT EXISTS collaborators(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  name TEXT NOT NULL,
                  email TEXT,
                  active INTEGER NOT NULL DEFAULT 1,
                  created_at TEXT NOT NULL
                );

                CREATE TABLE IF NOT EXISTS projects(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  client_id INTEGER NOT NULL,
                  name TEXT NOT NULL,
                  active INTEGER NOT NULL DEFAULT 1,
                  created_at TEXT NOT NULL,
                  UNIQUE(client_id, name),
                  FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE RESTRICT
                );

                CREATE TABLE IF NOT EXISTS hour_types(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  code TEXT NOT NULL UNIQUE,
                  description TEXT NOT NULL,
                  rate_value TEXT NOT NULL,
                  currency TEXT NOT NULL DEFAULT 'BRL',
                  active INTEGER NOT NULL DEFAULT 1,
                  created_at TEXT NOT NULL
                );

                CREATE TABLE IF NOT EXISTS time_entries(
                  id INTEGER PRIMARY KEY AUTOINCREMENT,
                  collaborator_id INTEGER NOT NULL,
                  project_id INTEGER NOT NULL,
                  hour_type_id INTEGER NOT NULL,
                  work_date TEXT NOT NULL,
                  hours TEXT NOT NULL,
                  notes TEXT,
                  created_at TEXT NOT NULL,
                  FOREIGN KEY (collaborator_id) REFERENCES collaborators(id) ON DELETE RESTRICT,
                  FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE RESTRICT,
                  FOREIGN KEY (hour_type_id) REFERENCES hour_types(id) ON DELETE RESTRICT
                );

                CREATE INDEX IF NOT EXISTS idx_entries_date ON time_entries(work_date);
                CREATE INDEX IF NOT EXISTS idx_entries_collab ON time_entries(collaborator_id);
                CREATE INDEX IF NOT EXISTS idx_entries_project ON time_entries(project_id);
                CREATE INDEX IF NOT EXISTS idx_entries_hourtype ON time_entries(hour_type_id);
                """;

        try (Connection c = open(); Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
