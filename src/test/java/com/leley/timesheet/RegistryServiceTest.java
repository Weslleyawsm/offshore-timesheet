package com.leley.timesheet;

import com.leley.timesheet.db.Database;
import com.leley.timesheet.service.RegistryService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class RegistryServiceTest {

    @Test
    void createsEntitiesAndEntry() {
        Database db = new Database("jdbc:sqlite::memory:", true);
        db.initSchema();

        RegistryService s = new RegistryService(db);

        long clientId = s.createClient("Cliente X");
        long projectId = s.createProject(clientId, "Projeto X");
        long collabId = s.createCollaborator("Fulano", "fulano@email.com");
        long hourTypeId = s.createHourType("NORMAL", "Hora normal", new BigDecimal("100"), "BRL");

        long entryId = s.createTimeEntry(collabId, projectId, hourTypeId, LocalDate.of(2026, 1, 1), new BigDecimal("8"), "ok");
        assertTrue(entryId > 0);
    }

    @Test
    void schemaWorks() {
        Database db = new Database("jdbc:sqlite::memory:", true);
        db.initSchema();
        try (Connection c = db.open(); Statement st = c.createStatement()) {
            st.executeQuery("SELECT 1");
        } catch (Exception e) {
            fail(e);
        }
    }
}
