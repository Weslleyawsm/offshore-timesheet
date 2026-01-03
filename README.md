# Offshore Timesheet

Sistema de lançamento de horas e geração de relatórios financeiros para contexto offshore, desenvolvido em Java puro, com foco em simplicidade, clareza de domínio e organização de código.

## Visão geral
Em ambientes offshore, é comum controlar horas trabalhadas por colaborador, projeto e tipo de hora, cada uma com valores distintos. Este sistema resolve esse problema permitindo:

- cadastro de entidades de negócio
- lançamento de horas por data e tipo
- geração de relatórios consolidados por período
- cálculo automático de valores financeiros
- exportação de dados para CSV

O projeto foi desenvolvido sem frameworks, priorizando JDBC, regras de negócio explícitas e controle transacional manual.

## Stack
- Java 17
- Maven
- JDBC
- SQLite
- JUnit 5
- CLI (aplicação em console)

## Funcionalidades

### Cadastros
- Clientes
- Projetos (associados a clientes)
- Colaboradores
- Tipos de hora (com valor/hora e moeda)

### Lançamento de horas
- Data trabalhada
- Colaborador
- Projeto
- Tipo de hora
- Quantidade de horas
- Observações opcionais

### Relatórios
- Resumo por colaborador (horas e valor total)
- Resumo por tipo de hora
- Relatório detalhado com filtros
- Total financeiro consolidado por período
- Exportação do relatório detalhado em CSV

## Arquitetura
O projeto segue uma separação clara de responsabilidades:

- `domain`: entidades de negócio
- `repo`: acesso a dados via JDBC
- `service`: regras de negócio e validações
- `db`: controle de conexão e transações
- `cli`: interface de console
- `util`: utilitários de data e valores monetários

Não há uso de frameworks ou ORMs, permitindo controle total sobre SQL, transações e fluxo da aplicação.

## Como executar

### Pré-requisitos
- Java 17 ou superior
- Maven 3.8+

### Executar testes
```bash
mvn test
