package com.leley.timesheet.service;

import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class CsvExporter {

    public void exportDetailed(String path, List<ReportingService.DetailedRow> rows) {
        try (FileWriter w = new FileWriter(path, StandardCharsets.UTF_8)) {
            w.write("date,collaborator,client,project,hourType,hours,rate,value,currency,notes\n");
            for (var r : rows) {
                w.write(csv(r.date().toString())); w.write(",");
                w.write(csv(r.collaborator())); w.write(",");
                w.write(csv(r.client())); w.write(",");
                w.write(csv(r.project())); w.write(",");
                w.write(csv(r.hourTypeCode())); w.write(",");
                w.write(csv(r.hours().toPlainString())); w.write(",");
                w.write(csv(r.rate().toPlainString())); w.write(",");
                w.write(csv(r.value().toPlainString())); w.write(",");
                w.write(csv(r.currency())); w.write(",");
                w.write(csv(r.notes())); w.write("\n");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String csv(String s) {
        if (s == null) return "";
        String v = s.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) return "\"" + v + "\"";
        return v;
    }
}
