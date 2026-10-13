# Sessão 2026-04-25 — Endpoint DELETE /expenses/{id}

## O que foi feito
- Implementado endpoint `DELETE /expenses/{id}` (204 No Content)
- Criada `ExpenseNotFoundException` com handler 404 no `GlobalExceptionHandler`
- Adicionado método `delete(UUID)` no `ExpenseService`
- Adicionado `DELETE` nos métodos CORS permitidos no `WebConfig`
- Deploy realizado no Render (auto-deploy via push no GitHub)

## Commits
- `a0495e0` — feat: add DELETE /expenses/{id} endpoint with 404 handling
- `3360fc2` — docs: update CLAUDE.md with session 2026-04-25 progress

## Observação
Nota original preservada do `CLAUDE.md` (seção "Sessão 2026-04-25", removida posteriormente no commit `e3a5722`).
