# Home Budget API

API REST para controle de gastos domésticos compartilhado entre dois usuários.

O projeto faz parte de um aplicativo de orçamento doméstico com backend em Spring Boot,
frontend web em Angular e futuro app nativo em Swift/iOS.

## Status

Backend completo e em produção no Render.

- Backend: `https://home-budget-api-wml2.onrender.com`
- Frontend web: `https://home-budget-web-ten.vercel.app`

## Stack

- Java 21
- Spring Boot 3.5
- Spring Web
- Spring Data JPA
- Bean Validation
- H2 em memória para desenvolvimento local
- PostgreSQL em produção
- Render para hospedagem

## Funcionalidades

- Criar, listar, atualizar e remover gastos
- Listar gastos por mês
- Classificar gastos como fixos ou variáveis
- Importar gastos fixos do mês anterior
- Registrar e consultar entrada mensal
- Compartilhar os mesmos dados entre dois usuários conhecidos

Não há autenticação no MVP. O app é de uso privado.

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

Base URL local:

```text
http://localhost:8080
```

Base URL de produção:

```text
https://home-budget-api-wml2.onrender.com
```

### Gastos

#### Criar gasto

```http
POST /expenses
```

Request:

```json
{
  "description": "Supermercado",
  "amount": 15000,
  "date": "2026-04-25",
  "category": "VARIABLE"
}
```

Response `201`:

```json
{
  "expenseId": "uuid",
  "description": "Supermercado",
  "amount": 15000,
  "date": "2026-04-25",
  "category": "VARIABLE",
  "createdAt": "2026-04-25T10:30:00Z"
}
```

#### Listar gastos do mês

```http
GET /expenses?month=2026-04
```

Response `200`:

```json
[
  {
    "expenseId": "uuid",
    "description": "Supermercado",
    "amount": 15000,
    "date": "2026-04-25",
    "category": "VARIABLE",
    "createdAt": "2026-04-25T10:30:00Z"
  }
]
```

Se `month` não for informado, a API usa o mês atual.

#### Atualizar gasto

```http
PUT /expenses/{id}
```

Request:

```json
{
  "description": "Aluguel",
  "amount": 250000,
  "date": "2026-04-01",
  "category": "FIXED"
}
```

Response `200`: gasto atualizado.

#### Remover gasto

```http
DELETE /expenses/{id}
```

Response `204`, sem body.

#### Importar gastos fixos

```http
POST /expenses/import-fixed?month=2026-06
```

Copia os gastos `FIXED` do mês anterior para o mês informado, usando o dia 1 como data.

Response `201`: array com os gastos criados.

### Entrada mensal

#### Buscar entrada do mês

```http
GET /income?month=2026-06
```

Response `200`:

```json
{
  "month": "2026-06",
  "amount": 500000
}
```

Se não houver entrada registrada, retorna `amount` igual a `0`.

#### Salvar ou atualizar entrada do mês

```http
PUT /income?month=2026-06
```

Request:

```json
{
  "amount": 500000
}
```

Response `200`:

```json
{
  "month": "2026-06",
  "amount": 500000
}
```

## Validações

- `description`: obrigatório e não vazio
- `amount` de gasto: obrigatório e positivo
- `amount` de entrada mensal: obrigatório e maior ou igual a zero
- `date`: obrigatório
- `category`: obrigatório, com valor `FIXED` ou `VARIABLE`

Erros de validação retornam `422`.

## CORS

Origens liberadas atualmente:

- `http://localhost:4200`
- `https://home-budget-web-ten.vercel.app`

Para adicionar uma nova origem, atualize `src/main/java/com/homebudget/WebConfig.java`.

## Executando localmente

Pré-requisitos:

- Java 21
- Maven

Rodar a API:

```bash
./mvnw spring-boot:run
```

Em desenvolvimento local, a aplicação usa H2 em memória. O banco é recriado a cada
startup.

Console H2:

```text
http://localhost:8080/h2-console
```

Configuração local do H2:

```text
JDBC URL: jdbc:h2:mem:orcamento
User: sa
Password: 
```

## Produção

O profile de produção usa PostgreSQL. As credenciais são lidas das variáveis de ambiente:

- `PGHOST`
- `PGPORT`
- `PGDATABASE`
- `PGUSER`
- `PGPASSWORD`

Configuração principal:

```properties
spring.datasource.url=jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}?sslmode=require
spring.jpa.hibernate.ddl-auto=update
```

## Testes

Rodar testes:

```bash
./mvnw test
```

## Convenções

- Valores monetários são armazenados em centavos.
- Datas são trafegadas em ISO 8601.
- Código e variáveis ficam em inglês.
- Commits devem ser escritos em inglês.
