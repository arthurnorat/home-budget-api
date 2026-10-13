# Histórico de passos — home-budget-api

Reconstrução do histórico de desenvolvimento deste backend, passo por passo, a partir do `git log`, dos diffs de cada commit e das notas de sessão que existiam em versões antigas do `CLAUDE.md` (removidas na sessão de 2026-06-10, ver [`step-10-income-endpoint.md`](step-10-income-endpoint.md)).

Pensado para ser importado no Claude.ai e estudado, no mesmo formato usado no acompanhamento do app iOS.

## Linha do tempo

1. [step-01 — Setup inicial do Spring Boot](step-01-spring-boot-initial-setup.md)
2. [step-02 — Atualização da stack: Angular + app iOS nativo](step-02-stack-update-angular-ios.md)
3. [step-03 — Configuração de PostgreSQL, CORS e tratamento de exceções](step-03-postgres-cors-exception-handling.md)
4. [step-04 — Migração do deploy para Render + Neon](step-04-render-neon-deploy-migration.md)
5. [step-05 — Endpoint DELETE /expenses/{id}](step-05-delete-expense-endpoint.md)
6. [step-06 — CORS para o Vercel de produção](step-06-cors-vercel-production.md)
7. [step-07 — Renome do projeto: orçamento → home-budget-api](step-07-project-rename.md)
8. [step-08 — Endpoint PUT /expenses/{id} e edição no frontend](step-08-put-expense-editing.md)
9. [step-09 — Importar fixos do mês anterior e filtro de categoria](step-09-import-fixed-category-filter.md)
10. [step-10 — Endpoint de entrada mensal (income) e simplificação do CLAUDE.md](step-10-income-endpoint.md)
11. [step-11 — README atualizado e AGENTS.md para o Codex](step-11-readme-agents-md-update.md)
12. [step-12 — Workflow para evitar cold start no Render e no Neon](step-12-keep-warm-workflow.md)

## Fontes
Todo o conteúdo acima foi reconstruído a partir de dados reais deste repositório — nenhuma sessão foi inventada:
- `git log --reverse` e `git show --stat` de cada commit
- `git show <hash>:CLAUDE.md` para recuperar o texto original das sessões documentadas entre 2026-04-25 e 2026-05-02
