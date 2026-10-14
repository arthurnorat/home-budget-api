# 0003 — Remover o keep-warm após esgotar a cota do Neon

- **Status:** Substituído por [0004](0004-reativar-keep-warm-temporario-a-cada-10-min.md) (2026-09-08)
- **Substitui:** [0002](0002-keep-warm-via-ping-externo.md)
- **Relacionado:** [0001](0001-hospedagem-no-render-e-neon.md)

## Contexto

Em **2026-08-19** o backend em produção passou a retornar `500` em todas as
rotas que tocam o banco.

Causa raiz: o projeto Neon `orcamento` (plano gratuito) excedeu a cota mensal
de compute (110+ CU-hrs consumidos entre 01/08 e 19/08) e foi **pausado
automaticamente** pelo próprio Neon.

O consumo veio dos dois jobs de keep-warm de [0002](0002-keep-warm-via-ping-externo.md)
pingando `/expenses` a cada 5 min, que mantinham o Neon ativo quase 24 h/dia.
Os e-mails do Render de "deploy failed" e "Exited with status 1" recebidos no
mesmo dia são sintoma dessa causa (health check falha porque o Postgres está
pausado), não bugs novos.

## Decisão

Vamos **desligar todo o keep-warm** e aceitar o cold start até termos uma
forma segura de manter o serviço quente:

- remover `.github/workflows/keep-warm.yml` (commit `05e8073`);
- desativar o cronjob no cron-job.org (marcado como **Inactive** em 2026-08-20).

Nenhum ping deve voltar a apontar para `/expenses` (ou qualquer rota que toque
o banco) num intervalo menor que o autosuspend do Neon (~5 min).

## Consequências

- **Bom:** o Neon deixa de ser mantido acordado à força; a cota de compute
  volta a ser respeitada. Confirmado em **2026-09-04** via `curl` que o ciclo
  gratuito resetou e `GET /expenses` voltou a responder `200`. Incidente
  encerrado.
- **Ruim:** volta o cold start — a primeira request após inatividade demora
  ~1 min (Render) mais a resumção do Neon.
- **Lição:** qualquer keep-warm futuro precisa ou (a) não tocar o banco, ou
  (b) ter intervalo maior que o autosuspend do Neon, para que o compute
  descanse entre os pings. Isso orienta os ADRs
  [0004](0004-reativar-keep-warm-temporario-a-cada-10-min.md) e
  [0005](0005-liveness-probe-com-actuator-para-keep-warm-seguro.md).
- Ação de acompanhamento em aberto: adicionar observabilidade de consumo do
  Neon para que uma anomalia dessas seja detectada pela causa, não pelo sintoma.
