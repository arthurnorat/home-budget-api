# Histórico de sessões — home-budget-api

Reconstrução do histórico de desenvolvimento deste backend, sessão por sessão, a partir do `git log`, dos diffs de cada commit e das notas de sessão que existiam em versões antigas do `CLAUDE.md` (removidas na sessão de 2026-06-10, ver [`2026-06-10-income-endpoint.md`](2026-06-10-income-endpoint.md)).

Pensado para ser importado no Claude.ai e estudado, no mesmo formato usado no acompanhamento do app iOS.

## Linha do tempo

1. [2026-03-03 — Setup inicial do Spring Boot](2026-03-03-setup-inicial-spring-boot.md)
2. [2026-03-04 — Atualização da stack: Angular + app iOS nativo](2026-03-04-atualizacao-stack-ios.md)
3. [2026-03-06 — Configuração de PostgreSQL, CORS e tratamento de exceções](2026-03-06-config-postgres-cors-exceptions.md)
4. [2026-04-24 — Migração do deploy para Render + Neon](2026-04-24-deploy-render-dockerfile.md)
5. [2026-04-25 — Endpoint DELETE /expenses/{id}](2026-04-25-delete-expense.md)
6. [2026-04-26 — CORS para o Vercel de produção](2026-04-26-cors-vercel.md)
7. [2026-04-27 — Renome do projeto: orçamento → home-budget-api](2026-04-27-rename-projeto.md)
8. [2026-04-28 — Endpoint PUT /expenses/{id} e edição no frontend](2026-04-28-put-expense-edicao.md)
9. [2026-05-02 — Importar fixos do mês anterior e filtro de categoria](2026-05-02-import-fixed-filtro-categoria.md)
10. [2026-06-10 — Endpoint de entrada mensal (income) e simplificação do CLAUDE.md](2026-06-10-income-endpoint.md)
11. [2026-06-29 — README atualizado e AGENTS.md para o Codex](2026-06-29-docs-agents-readme.md)
12. [2026-07-16 — Workflow para evitar cold start no Render e no Neon](2026-07-16-keep-warm-workflow.md)

## Fontes
Todo o conteúdo acima foi reconstruído a partir de dados reais deste repositório — nenhuma sessão foi inventada:
- `git log --reverse` e `git show --stat` de cada commit
- `git show <hash>:CLAUDE.md` para recuperar o texto original das sessões documentadas entre 2026-04-25 e 2026-05-02
