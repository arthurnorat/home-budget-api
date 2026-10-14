# 0001 — Hospedar a API no Render e o Postgres no Neon

- **Status:** Aceito (2026-04-24)
- **Relacionado:** [0002](0002-keep-warm-via-ping-externo.md), [0006](0006-self-host-do-postgres-em-servidor-domestico.md)

## Contexto

O projeto é um app de controle de gastos de uso privado entre duas pessoas,
com finalidade de aprendizado. Não há orçamento para infraestrutura — tudo
precisa caber em planos gratuitos.

O plano inicial era hospedar na Railway, mas o crédito gratuito da Railway
deixou de ser suficiente para manter o serviço no ar de forma contínua.

Necessidades:

- rodar um container Java (Spring Boot) exposto por HTTP;
- um PostgreSQL gerenciado, com backup e SSL;
- deploy automático a partir de um push no GitHub;
- custo zero.

## Decisão

Vamos separar em dois provedores gratuitos:

- **Render** hospeda a API. Deploy via `Dockerfile` multi-stage (build com
  `eclipse-temurin:21-jdk-alpine` + Maven Wrapper, imagem final com JRE),
  disparado automaticamente a cada push na branch `main`.
- **Neon** hospeda o PostgreSQL. A conexão exige `?sslmode=require` na URL
  JDBC. As credenciais (`PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`,
  `PGPASSWORD`) são configuradas **manualmente** nas variáveis de ambiente do
  Render — o Render não injeta credenciais de banco automaticamente como a
  Railway fazia.

URLs de produção:

- API: `https://home-budget-api-wml2.onrender.com`
- Frontend (Vercel): `https://home-budget-web-ten.vercel.app`

## Consequências

- **Bom:** custo zero; deploy automático; banco gerenciado com SSL e backup.
- **Ruim:** dois painéis para administrar em vez de um; as credenciais do
  banco vivem só nas variáveis de ambiente do Render, fora do controle de
  versão (ver `docs/operations.md`).
- **Risco (planos gratuitos):**
  - o serviço web do Render **hiberna após ~15 min** sem tráfego → cold start
    de ~30–60 s na primeira request seguinte;
  - o compute do Neon **suspende após ~5 min** sem query (scale-to-zero, fixo
    no plano gratuito) e tem **cota mensal de compute** (~190 h) que, se
    excedida, **pausa o banco** até o ciclo seguinte.
  - Esses dois riscos motivam os ADRs [0002](0002-keep-warm-via-ping-externo.md)
    em diante.
