package com.leley.timesheet;

import com.leley.timesheet.db.Database;
import com.leley.timesheet.service.RegistryService;
import com.leley.timesheet.service.ReportingService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ReportingServiceTest {

    @Test
    void aggregatesTotals() {
        Database db = new Database("jdbc:sqlite::memory:", true);
        db.initSchema();

        RegistryService reg = new RegistryService(db);
        ReportingService rep = new ReportingService(db);

        long clientId = reg.createClient("C1");
        long projectId = reg.createProject(clientId, "P1");
        long collabId = reg.createCollaborator("A", null);
        long htNormal = reg.createHourType("NORMAL", "Normal", new BigDecimal("100"), "BRL");
        long htExtra = reg.createHourType("EXTRA50", "Extra", new BigDecimal("150"), "BRL");

        reg.createTimeEntry(collabId, projectId, htNormal, LocalDate.of(2026, 1, 1), new BigDecimal("8"), null);
        reg.createTimeEntry(collabId, projectId, htExtra, LocalDate.of(2026, 1, 2), new BigDecimal("2"), null);

        BigDecimal total = rep.totalValue(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        assertEquals(0, total.compareTo(new BigDecimal("1100")));
    }
}
