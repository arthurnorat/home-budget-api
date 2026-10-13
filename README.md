🇺🇸 [English](#english) · 🇧🇷 [Português](#português)

---

<a name="english"></a>

# Home Budget API

REST API for shared household expense tracking between two users.

This backend is built with Spring Boot. It is shared by a web client, a
macOS client, and dedicated iOS and Android clients for quick expense entry.

## Live demo

- Web app: `https://home-budget-web-ten.vercel.app`
- API: `https://home-budget-api-wml2.onrender.com`

## Features

- Create, list, update and delete expenses
- List expenses by month
- Classify expenses as fixed or variable
- Import fixed expenses from the previous month
- Register and query monthly income
- Share the same data between two known users

There is no authentication in the MVP. The app is for private use.

## Stack

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Bean Validation
- In-memory H2 for local development
- PostgreSQL (Neon) in production
- Render for hosting

## Architecture

- Layered REST API: controllers only handle HTTP, business logic lives in
  services, data access goes through Spring Data JPA repositories; entities
  and DTOs are kept separate.
- Domain exceptions are mapped to HTTP responses in a single
  `@RestControllerAdvice`.
- Code organized by feature package (`expense`, `income`).

## Data model

### `expenses`

| Field | Type | Note |
| --- | --- | --- |
| `expense_id` | UUID | Automatically generated primary key |
| `description` | string | Required |
| `amount` | integer | Amount in cents |
| `date` | date | Expense date in ISO 8601 (`YYYY-MM-DD`) |
| `category` | enum | `FIXED` or `VARIABLE` |
| `created_at` | timestamp | Generated automatically |

### `monthly_income`

| Field | Type | Note |
| --- | --- | --- |
| `month` | string | Month in `yyyy-MM` format |
| `amount` | integer | Monthly income in cents |

## API

- `POST /expenses` — create an expense
- `GET /expenses?month=yyyy-MM` — list expenses for a month (defaults to the current month)
- `PUT /expenses/{id}` — update an expense
- `DELETE /expenses/{id}` — delete an expense
- `POST /expenses/import-fixed?month=yyyy-MM` — copy the previous month's fixed expenses
- `GET /income?month=yyyy-MM` — get the month's income
- `PUT /income?month=yyyy-MM` — save or update the month's income

Full request/response contract: see the project's `CLAUDE.md`.

## Conventions

- Monetary values are stored in cents.
- Dates are exchanged in ISO 8601.
- Code and variables are in English.
- Commits must be written in English.

---

<a name="português"></a>

# Home Budget API

API REST para controle de gastos domésticos compartilhado entre dois usuários.

Este backend é feito em Spring Boot. É compartilhado por um cliente web, um
cliente macOS, e clientes dedicados em iOS e Android para inserção rápida de
gastos.

## Demo em produção

- App web: `https://home-budget-web-ten.vercel.app`
- API: `https://home-budget-api-wml2.onrender.com`

## Funcionalidades

- Criar, listar, atualizar e remover gastos
- Listar gastos por mês
- Classificar gastos como fixos ou variáveis
- Importar gastos fixos do mês anterior
- Registrar e consultar entrada mensal
- Compartilhar os mesmos dados entre dois usuários conhecidos

Não há autenticação no MVP. O app é de uso privado.

## Stack

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Bean Validation
- H2 em memória para desenvolvimento local
- PostgreSQL (Neon) em produção
- Render para hospedagem

## Arquitetura

- API REST em camadas: controllers só lidam com HTTP, lógica de negócio nos
  services, acesso a dados via repositories do Spring Data JPA; entidades e
  DTOs ficam separados.
- Exceções de domínio são mapeadas para respostas HTTP num único
  `@RestControllerAdvice`.
- Código organizado por feature (`expense`, `income`).

## Modelo de dados

### `expenses`

| Campo | Tipo | Observação |
| --- | --- | --- |
| `expense_id` | UUID | Chave primária gerada automaticamente |
| `description` | string | Obrigatório |
| `amount` | integer | Valor em centavos |
| `date` | date | Data do gasto em ISO 8601 (`YYYY-MM-DD`) |
| `category` | enum | `FIXED` ou `VARIABLE` |
| `created_at` | timestamp | Criado automaticamente |

### `monthly_income`

| Campo | Tipo | Observação |
| --- | --- | --- |
| `month` | string | Mês no formato `yyyy-MM` |
| `amount` | integer | Entrada mensal em centavos |

## API

- `POST /expenses` — cria um gasto
- `GET /expenses?month=yyyy-MM` — lista os gastos do mês (padrão: mês atual)
- `PUT /expenses/{id}` — atualiza um gasto
- `DELETE /expenses/{id}` — remove um gasto
- `POST /expenses/import-fixed?month=yyyy-MM` — copia os gastos fixos do mês anterior
- `GET /income?month=yyyy-MM` — busca a entrada do mês
- `PUT /income?month=yyyy-MM` — salva ou atualiza a entrada do mês

Contrato completo de request/response: ver o `CLAUDE.md` do projeto.

## Convenções

- Valores monetários são armazenados em centavos.
- Datas são trafegadas em ISO 8601.
- Código e variáveis ficam em inglês.
- Commits devem ser escritos em inglês.
