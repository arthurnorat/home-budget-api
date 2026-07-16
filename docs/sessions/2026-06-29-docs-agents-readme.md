# Sessão 2026-06-29 — README atualizado e AGENTS.md para o Codex

## O que foi feito
- `AGENTS.md` criado, replicando o contexto/contrato de API do `CLAUDE.md` para uso pelo Codex (import do contrato via `@import`)
- `README.md` atualizado com detalhes atuais do projeto (287 linhas alteradas — reescrita substancial cobrindo stack, endpoints e status do projeto)
- Adicionado ao `AGENTS.md` um "Fluxo de Alterações": o agente deve apresentar o plano/diff proposto e aguardar autorização explícita antes de editar, criar ou remover arquivos, mantendo o menor escopo necessário após aprovado

## Commits
- `0244456` — Add AGENTS.md instructions for Codex
- `b1fd522` — Updates README.md with current details of the project
- `2d4a18a` — Add approval workflow to AGENTS instructions

## Observação
Sessão administrativa/documental — não altera código de produção, mas formaliza como agentes de IA (Codex, e por extensão outras ferramentas) devem operar neste repositório.
