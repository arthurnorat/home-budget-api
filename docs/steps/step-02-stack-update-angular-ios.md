# Sessão 2026-03-04 — Atualização da stack: Angular + app iOS nativo

## O que foi feito
- Revisado o plano de stack no `CLAUDE.md`, simplificando o escopo do projeto:
  - Frontend web trocado de **React** para **Angular**
  - Removidos os planos de **React Native** (app mobile único) e **Kotlin** (app Android nativo)
  - Definido um único app nativo futuro: **Swift/iOS**, hospedado no iPhone
  - "Ordem de Construção" atualizada para refletir só 3 etapas: Backend → Frontend web (Angular) → App iOS (Swift)
- Nenhuma mudança de código nesta sessão — só decisão de arquitetura documentada

## Commits
- `4f9abb7` — docs: update stack to Angular and add iOS native app

## Observação
Esta decisão reduziu o escopo do projeto de "app único multiplataforma + apps nativos duplos" para "web (Angular) + um único app nativo (iOS)", o que se manteve estável até hoje.
