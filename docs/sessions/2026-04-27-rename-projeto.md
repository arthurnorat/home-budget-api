# Sessão 2026-04-27 — Renome do projeto: orçamento → home-budget-api

## O que foi feito
- Diretório renomeado: `~/Java_Projects/orcamento` → `~/Java_Projects/home-budget-api`
- Pacote Java migrado de `com.orcamento` para `com.homebudget` (commit `9b5ed47`)
- Repositório GitHub renomeado de `orcamento-domestico` para `home-budget-api`
- Remote git local atualizado para `https://github.com/arthurnorat/home-budget-api.git`
- Referências antigas ao nome `orcamento` removidas dos arquivos `.idea/` do IntelliJ
- Corrigido nome do módulo IntelliJ (`[orcamento]` → `home-budget-api`) editando o cache externo em `~/Library/Caches/JetBrains/IntelliJIdea2026.1/projects/home-budget-api.d764aca5/external_build_system/`
- Criado `home-budget-api.iml` na raiz do projeto

## Commits
- `bd2b6e7` — docs: add directory rename as first task for next session
- `9b5ed47` — refactor: rename package from com.orcamento to com.homebudget
- `5f2cb1f` — docs: update CLAUDE.md with session 2026-04-27 progress

## Observação
Nota original preservada do `CLAUDE.md` (seção "Sessão 2026-04-27", removida posteriormente no commit `e3a5722`). O problema do nome de módulo do IntelliJ que persistia após o renome está documentado com mais detalhe na memória `project_intellij_module_rename`.
