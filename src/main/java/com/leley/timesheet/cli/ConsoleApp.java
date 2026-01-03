package com.leley.timesheet.cli;

import com.leley.timesheet.db.Database;
import com.leley.timesheet.domain.Client;
import com.leley.timesheet.domain.Collaborator;
import com.leley.timesheet.domain.HourType;
import com.leley.timesheet.domain.Project;
import com.leley.timesheet.service.CsvExporter;
import com.leley.timesheet.service.RegistryService;
import com.leley.timesheet.service.ReportingService;

import java.time.LocalDate;
import java.util.List;

public final class ConsoleApp {
    private final ConsoleIO io = new ConsoleIO();
    private final RegistryService registry;
    private final ReportingService reporting;
    private final CsvExporter exporter = new CsvExporter();

    public ConsoleApp(Database db) {
        this.registry = new RegistryService(db);
        this.reporting = new ReportingService(db);
    }

    public void run() {
        seedIfEmpty();

        while (true) {
            io.println("");
            io.println("=== Offshore Timesheet ===");
            io.println("1) Cadastros");
            io.println("2) Lançar horas");
            io.println("3) Relatórios");
            io.println("0) Sair");

            String op = io.readLine("Opção").trim();
            if (op.equals("0")) break;
            if (op.equals("1")) menuCadastros();
            else if (op.equals("2")) menuLancamentos();
            else if (op.equals("3")) menuRelatorios();
            else io.println("Opção inválida.");
        }
    }

    private void menuCadastros() {
        while (true) {
            io.println("");
            io.println("=== Cadastros ===");
            io.println("1) Cliente");
            io.println("2) Projeto");
            io.println("3) Colaborador");
            io.println("4) Tipo de hora");
            io.println("5) Listar tudo");
            io.println("0) Voltar");

            String op = io.readLine("Opção").trim();
            if (op.equals("0")) break;

            switch (op) {
                case "1" -> cadastrarCliente();
                case "2" -> cadastrarProjeto();
                case "3" -> cadastrarColaborador();
                case "4" -> cadastrarTipoHora();
                case "5" -> listarCadastros();
                default -> io.println("Opção inválida.");
            }
        }
    }

    private void menuLancamentos() {
        while (true) {
            io.println("");
            io.println("=== Lançamento de Horas ===");
            io.println("1) Novo lançamento");
            io.println("0) Voltar");

            String op = io.readLine("Opção").trim();
            if (op.equals("0")) break;
            if (op.equals("1")) lancarHoras();
            else io.println("Opção inválida.");
        }
    }

    private void menuRelatorios() {
        while (true) {
            io.println("");
            io.println("=== Relatórios ===");
            io.println("1) Resumo por colaborador (período)");
            io.println("2) Resumo por tipo de hora (período)");
            io.println("3) Detalhado + exportar CSV");
            io.println("0) Voltar");

            String op = io.readLine("Opção").trim();
            if (op.equals("0")) break;

            switch (op) {
                case "1" -> relResumoColaborador();
                case "2" -> relResumoTipoHora();
                case "3" -> relDetalhadoCsv();
                default -> io.println("Opção inválida.");
            }
        }
    }

    private void cadastrarCliente() {
        try {
            String name = io.readLine("Nome do cliente");
            long id = registry.createClient(name);
            io.println("Cliente criado. ID: " + id);
        } catch (Exception e) {
            io.println(e.getMessage());
        }
    }

    private void cadastrarColaborador() {
        try {
            String name = io.readLine("Nome do colaborador");
            String email = io.readLine("Email (opcional)");
            long id = registry.createCollaborator(name, email);
            io.println("Colaborador criado. ID: " + id);
        } catch (Exception e) {
            io.println(e.getMessage());
        }
    }

    private void cadastrarProjeto() {
        try {
            List<Client> clients = registry.listClients();
            if (clients.isEmpty()) {
                io.println("Cadastre um cliente primeiro.");
                return;
            }
            io.println("Clientes:");
            for (Client c : clients) io.println(c.id() + " - " + c.name());

            long clientId = io.readLong("Client ID");
            String name = io.readLine("Nome do projeto");
            long id = registry.createProject(clientId, name);
            io.println("Projeto criado. ID: " + id);
        } catch (Exception e) {
            io.println(e.getMessage());
        }
    }

    private void cadastrarTipoHora() {
        try {
            String code = io.readLine("Código (ex: NORMAL, EXTRA50)");
            String desc = io.readLine("Descrição");
            var rate = io.readBigDecimal("Valor/hora");
            String currency = io.readLine("Moeda (ex: BRL)");
            long id = registry.createHourType(code, desc, rate, currency);
            io.println("Tipo de hora criado. ID: " + id);
        } catch (Exception e) {
            io.println(e.getMessage());
        }
    }

    private void listarCadastros() {
        List<Client> clients = registry.listClients();
        List<Project> projects = registry.listProjects(false);
        List<Collaborator> collabs = registry.listCollaborators(false);
        List<HourType> types = registry.listHourTypes(false);

        io.println("");
        io.println("Clientes:");
        for (Client c : clients) io.println(c.id() + " - " + c.name());

        io.println("");
        io.println("Projetos:");
        for (Project p : projects) io.println(p.id() + " - clientId=" + p.clientId() + " - " + p.name() + " - active=" + p.active());

        io.println("");
        io.println("Colaboradores:");
        for (Collaborator c : collabs) io.println(c.id() + " - " + c.name() + " - " + (c.email() == null ? "" : c.email()) + " - active=" + c.active());

        io.println("");
        io.println("Tipos de hora:");
        for (HourType t : types) io.println(t.id() + " - " + t.code() + " - " + t.description() + " - " + t.rateValue() + " " + t.currency() + " - active=" + t.active());
    }

    private void lancarHoras() {
        try {
            List<Collaborator> collabs = registry.listCollaborators(true);
            List<Project> projects = registry.listProjects(true);
            List<HourType> types = registry.listHourTypes(true);

            if (collabs.isEmpty() || projects.isEmpty() || types.isEmpty()) {
                io.println("Cadastros incompletos. Verifique colaborador, projeto e tipo de hora ativos.");
                return;
            }

            io.println("Colaboradores ativos:");
            for (Collaborator c : collabs) io.println(c.id() + " - " + c.name());

            io.println("Projetos ativos:");
            for (Project p : projects) io.println(p.id() + " - " + p.name() + " (clientId=" + p.clientId() + ")");

            io.println("Tipos de hora ativos:");
            for (HourType t : types) io.println(t.id() + " - " + t.code() + " - " + t.rateValue() + " " + t.currency());

            long collaboratorId = io.readLong("Collaborator ID");
            long projectId = io.readLong("Project ID");
            long hourTypeId = io.readLong("HourType ID");
            LocalDate date = io.readDate("Data (YYYY-MM-DD)");
            var hours = io.readBigDecimal("Horas (ex: 8, 1.5)");
            String notes = io.readLine("Observação (opcional)");

            long id = registry.createTimeEntry(collaboratorId, projectId, hourTypeId, date, hours, notes);
            io.println("Lançamento criado. ID: " + id);
        } catch (Exception e) {
            io.println(e.getMessage());
        }
    }

    private void relResumoColaborador() {
        try {
            LocalDate from = io.readDate("Data inicial");
            LocalDate to = io.readDate("Data final");

            var list = reporting.summaryByCollaborator(from, to);
            var total = reporting.totalValue(from, to);

            io.println("");
            io.println("Resumo por colaborador:");
            for (var r : list) {
                io.println(r.collaboratorId() + " - " + r.collaboratorName() + " | horas=" + r.totalHours() + " | valor=" + r.totalValue());
            }
            io.println("Total geral (valor): " + total);
        } catch (Exception e) {
            io.println(e.getMessage());
        }
    }

    private void relResumoTipoHora() {
        try {
            LocalDate from = io.readDate("Data inicial");
            LocalDate to = io.readDate("Data final");

            var list = reporting.summaryByHourType(from, to);
            var total = reporting.totalValue(from, to);

            io.println("");
            io.println("Resumo por tipo de hora:");
            for (var r : list) {
                io.println(r.hourTypeId() + " - " + r.code() + " | horas=" + r.totalHours() + " | valor=" + r.totalValue() + " " + r.currency());
            }
            io.println("Total geral (valor): " + total);
        } catch (Exception e) {
            io.println(e.getMessage());
        }
    }

    private void relDetalhadoCsv() {
        try {
            LocalDate from = io.readDate("Data inicial");
            LocalDate to = io.readDate("Data final");

            Long collaboratorId = io.confirm("Filtrar por colaborador?") ? io.readLong("Collaborator ID") : null;
            Long projectId = io.confirm("Filtrar por projeto?") ? io.readLong("Project ID") : null;
            Long hourTypeId = io.confirm("Filtrar por tipo de hora?") ? io.readLong("HourType ID") : null;

            var rows = reporting.detailed(from, to, collaboratorId, projectId, hourTypeId);

            io.println("");
            io.println("Detalhado:");
            for (var r : rows) {
                io.println(r.date() + " | " + r.collaborator() + " | " + r.client() + " | " + r.project() + " | " + r.hourTypeCode()
                        + " | h=" + r.hours() + " | rate=" + r.rate() + " | value=" + r.value() + " " + r.currency()
                        + (r.notes() == null ? "" : " | " + r.notes()));
            }

            if (io.confirm("Exportar CSV?")) {
                String path = io.readLine("Caminho do arquivo (ex: relatorio.csv)");
                exporter.exportDetailed(path, rows);
                io.println("CSV gerado: " + path);
            }
        } catch (Exception e) {
            io.println(e.getMessage());
        }
    }

    private void seedIfEmpty() {
        var clients = registry.listClients();
        if (!clients.isEmpty()) return;

        long c1 = registry.createClient("Oceanic Services");
        long c2 = registry.createClient("BlueRig Offshore");

        long p1 = registry.createProject(c1, "Manutencao Plataforma A");
        long p2 = registry.createProject(c2, "Comissionamento Sistema B");

        long col1 = registry.createCollaborator("Carlos Silva", "carlos@empresa.com");
        long col2 = registry.createCollaborator("Ana Souza", "ana@empresa.com");

        long ht1 = registry.createHourType("NORMAL", "Hora normal", new java.math.BigDecimal("120"), "BRL");
        long ht2 = registry.createHourType("EXTRA50", "Hora extra 50%", new java.math.BigDecimal("180"), "BRL");

        registry.createTimeEntry(col1, p1, ht1, LocalDate.now().minusDays(3), new java.math.BigDecimal("8"), "Turno diurno");
        registry.createTimeEntry(col1, p1, ht2, LocalDate.now().minusDays(2), new java.math.BigDecimal("2"), "Ajuste de emergencia");
        registry.createTimeEntry(col2, p2, ht1, LocalDate.now().minusDays(2), new java.math.BigDecimal("8"), null);
    }
}
