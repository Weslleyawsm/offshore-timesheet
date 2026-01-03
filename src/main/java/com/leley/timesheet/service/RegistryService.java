package com.leley.timesheet.service;

import com.leley.timesheet.db.Database;
import com.leley.timesheet.db.Tx;
import com.leley.timesheet.domain.*;
import com.leley.timesheet.repo.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public final class RegistryService {
    private final Database db;
    private final ClientRepository clients = new ClientRepository();
    private final CollaboratorRepository collaborators = new CollaboratorRepository();
    private final ProjectRepository projects = new ProjectRepository();
    private final HourTypeRepository hourTypes = new HourTypeRepository();
    private final TimeEntryRepository entries = new TimeEntryRepository();

    public RegistryService(Database db) {
        this.db = db;
    }

    public long createClient(String name) {
        String n = Validation.required(name, "Nome do cliente");
        return Tx.inTx(db, c -> clients.create(c, n));
    }

    public long createCollaborator(String name, String email) {
        String n = Validation.required(name, "Nome do colaborador");
        String e = email == null ? null : email.trim();
        return Tx.inTx(db, c -> collaborators.create(c, n, (e != null && e.isBlank()) ? null : e));
    }

    public long createProject(long clientId, String name) {
        String n = Validation.required(name, "Nome do projeto");
        return Tx.inTx(db, c -> {
            if (clients.findById(c, clientId).isEmpty()) throw new IllegalArgumentException("Cliente não encontrado");
            return projects.create(c, clientId, n);
        });
    }

    public long createHourType(String code, String description, BigDecimal rateValue, String currency) {
        String cd = Validation.required(code, "Código");
        String desc = Validation.required(description, "Descrição");
        BigDecimal rate = Validation.positive(rateValue, "Valor/hora");
        String cur = Validation.required(currency, "Moeda");
        return Tx.inTx(db, c -> hourTypes.create(c, cd, desc, rate, cur));
    }

    public long createTimeEntry(long collaboratorId, long projectId, long hourTypeId, LocalDate workDate, BigDecimal hours, String notes) {
        if (workDate == null) throw new IllegalArgumentException("Data é obrigatória");
        BigDecimal h = Validation.positive(hours, "Horas");
        String nt = notes == null ? null : notes.trim();
        return Tx.inTx(db, c -> {
            if (collaborators.findById(c, collaboratorId).isEmpty()) throw new IllegalArgumentException("Colaborador não encontrado");
            if (projects.findById(c, projectId).isEmpty()) throw new IllegalArgumentException("Projeto não encontrado");
            Optional<HourType> ht = hourTypes.findById(c, hourTypeId);
            if (ht.isEmpty()) throw new IllegalArgumentException("Tipo de hora não encontrado");
            if (!ht.get().active()) throw new IllegalArgumentException("Tipo de hora inativo");
            return entries.create(c, collaboratorId, projectId, hourTypeId, workDate, h, (nt != null && nt.isBlank()) ? null : nt);
        });
    }

    public void deactivateCollaborator(long id) {
        Tx.inTx(db, c -> { collaborators.setActive(c, id, false); return null; });
    }

    public void deactivateProject(long id) {
        Tx.inTx(db, c -> { projects.setActive(c, id, false); return null; });
    }

    public void deactivateHourType(long id) {
        Tx.inTx(db, c -> { hourTypes.setActive(c, id, false); return null; });
    }

    public List<Client> listClients() {
        return Tx.inTx(db, clients::list);
    }

    public List<Collaborator> listCollaborators(boolean onlyActive) {
        return Tx.inTx(db, c -> collaborators.list(c, onlyActive));
    }

    public List<Project> listProjects(boolean onlyActive) {
        return Tx.inTx(db, c -> projects.list(c, onlyActive));
    }

    public List<Project> listProjectsByClient(long clientId, boolean onlyActive) {
        return Tx.inTx(db, c -> projects.listByClient(c, clientId, onlyActive));
    }

    public List<HourType> listHourTypes(boolean onlyActive) {
        return Tx.inTx(db, c -> hourTypes.list(c, onlyActive));
    }
}
