# Sessão 2026-03-06 — Configuração de PostgreSQL, CORS e tratamento de exceções

## O que foi feito
- Ajustada a URL JDBC de produção para usar variáveis individuais do Postgres (`PGHOST`, `PGPORT`, `PGDATABASE`) em vez de uma única `DATABASE_URL`, já pensando na infraestrutura da Railway
- Adicionado `spring.jpa.show-sql=false` ao perfil de produção
- Corrigido o driver de produção: `spring.datasource.driver-class-name` sobrescrito explicitamente para `org.postgresql.Driver`, evitando conflito com o driver H2 usado localmente
- Criado o `GlobalExceptionHandler` (`@RestControllerAdvice`) para capturar `MethodArgumentNotValidException` e devolver erros de validação formatados com status 422
- Criado o `WebConfig` com a primeira configuração global de CORS, liberando `http://localhost:4200` (Angular local) e a URL do Vercel do frontend, com métodos `GET` e `POST`

## Commits
- `a8b56f1` — fix: use individual PG vars for JDBC URL in prod config
- `b9db793` — fix: override H2 driver with PostgreSQL driver in prod config
- `d69478f` — feat: add global exception handler for validation errors
- `d746224` — feat: add global CORS configuration for Angular frontend

## Observação
Sessão de ajustes de infraestrutura/config, sem novos endpoints — provavelmente motivada por tentativas de deploy/integração com o frontend Angular que estava sendo desenvolvido em paralelo.
