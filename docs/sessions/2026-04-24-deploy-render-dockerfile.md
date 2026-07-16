# Sessão 2026-04-24 — Migração do deploy para Render + Neon

## O que foi feito
- Atualizado `application-prod.properties`: comentário e configuração migrados de "Railway" para "Render + Neon", e adicionado `?sslmode=require` na URL JDBC (exigência da conexão SSL do Neon PostgreSQL)
- As variáveis `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` passaram a ser configuradas manualmente nas variáveis de ambiente do Render (em vez de injetadas automaticamente, como seria na Railway)
- Criado o `Dockerfile` multi-stage para o deploy no Render: build com `eclipse-temurin:21-jdk-alpine` + Maven Wrapper, e imagem final com `eclipse-temurin:21-jre-alpine` rodando `app.jar` na porta 8080

## Commits
- `8668eac` — fix: add SSL requirement for Neon PostgreSQL connection
- `40262e6` — feat: add Dockerfile for Render deployment

## Observação
Marca a troca definitiva de provedor de hospedagem: de Railway (planejado desde a sessão inicial) para Render (backend) + Neon (banco PostgreSQL), que é a configuração usada em produção até hoje.
