# Offshore Timesheet (Java puro)

Sistema de lançamento de horas e geração de relatórios para contexto offshore.

## Features
- Cadastro de clientes, projetos, colaboradores, tipos de hora (valor/hora)
- Lançamento de horas por data, projeto e tipo de hora
- Relatórios por período:
  - Resumo por colaborador
  - Resumo por tipo de hora
  - Detalhado com filtros
- Exportação CSV do relatório detalhado
- Persistência SQLite via JDBC

## Requisitos
- Java 17+
- Maven 3.8+

## Rodar
```bash
mvn test
mvn package
java -jar target/offshore-timesheet-1.0.0.jar
```

## Banco
Por padrão usa `timesheet.db` na raiz do projeto (SQLite). Config em `src/main/resources/app.properties`.

## Relatórios
Os relatórios calculam:
- total de horas no período
- total de valor no período (horas * valor/hora)
