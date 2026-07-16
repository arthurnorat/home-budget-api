# Sessão 2026-03-03 — Setup inicial do Spring Boot

## O que foi feito
- Criado o projeto Spring Boot com Maven Wrapper (`mvnw`, `pom.xml`, `.mvn/wrapper`)
- Definida a entidade `Expense` (`expense_id`, `description`, `amount`, `date`, `category`, `created_at`) e o enum `Category` (`FIXED`/`VARIABLE`)
- Criados `ExpenseController`, `ExpenseService` e `ExpenseRepository` (Spring Data JPA)
- Criados os DTOs `ExpenseRequest` e `ExpenseResponse`
- Configurado `application.properties` (perfil local, provavelmente H2) e `application-prod.properties` (perfil de produção, na época pensado para Railway)
- Criado o `CLAUDE.md` inicial com o contexto do projeto: nesta versão a stack planejada ainda era **Railway** (hospedagem), **React** (frontend web) e, como planos futuros, **React Native** (app mobile único) e **Kotlin** (app nativo Android) além do Swift/iOS — esse plano mudaria já na sessão seguinte
- Criado `README.md` inicial e `.gitignore`/`.gitattributes`

## Commits
- `d833987` — feat: create initial Spring Boot project structure

## Observação
Esta é a primeira sessão do projeto. O pacote Java inicial era `com.orcamento` (renomeado para `com.homebudget` só na sessão de 2026-04-27).
