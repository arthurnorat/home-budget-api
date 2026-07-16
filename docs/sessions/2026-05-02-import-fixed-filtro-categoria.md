# Sessão 2026-05-02 — Importar fixos do mês anterior e filtro de categoria

## O que foi feito
- Frontend: adicionado filtro de categoria na `ExpenseTable` (Variável / Fixo / Todos) com padrão "Variável"
- Frontend: coluna de categoria removida da tabela (redundante com o filtro)
- Frontend: ajustes de responsividade mobile (padding, font-size, text-overflow)
- Backend: implementado endpoint `POST /expenses/import-fixed?month=yyyy-MM`
- Backend: adicionado `findByCategoryAndDateBetweenOrderByDateDesc` no `ExpenseRepository`
- Backend: adicionado método `importFixed(YearMonth)` no `ExpenseService`
- Frontend: adicionado botão "Importar" na filter bar que copia os gastos fixos do mês anterior
- `CLAUDE.md` do backend expandido com o contrato completo da API (para uso pelos projetos mobile via `@import`)

## Commits
- `4e7daba` — feat: add POST /expenses/import-fixed endpoint to copy previous month fixed expenses

## Observação
Nota original preservada do `CLAUDE.md` (seção "Sessão 2026-05-02", removida posteriormente no commit `e3a5722`). Esta sessão também formalizou o `CLAUDE.md` como "fonte de verdade" do contrato de API, pensando na futura integração com o app iOS.
