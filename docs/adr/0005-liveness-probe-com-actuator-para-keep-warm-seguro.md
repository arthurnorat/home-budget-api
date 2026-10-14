# 0005 — Liveness probe com Actuator para keep-warm seguro

- **Status:** Proposto
- **Substituirá:** [0004](0004-reativar-keep-warm-temporario-a-cada-10-min.md) quando implementado
- **Relacionado:** [0003](0003-remover-keep-warm-apos-incidente-de-cota.md)

## Contexto

O keep-warm de [0004](0004-reativar-keep-warm-temporario-a-cada-10-min.md)
funciona, mas ainda bate em `/expenses`, ou seja, ainda acorda o Neon a cada
ciclo — só que com intervalo grande o bastante para não estourar a cota.

O cenário ideal é manter **apenas o Render** quente e deixar o Neon dormir de
verdade, acordando somente quando alguém realmente usa o app. Para isso o ping
precisa acertar um endpoint que **não toca o banco**.

O Spring Boot Actuator oferece exatamente isso: `/actuator/health/liveness`,
que responde sobre o estado do processo da aplicação sem consultar o
datasource.

## Decisão

Vamos adicionar o Actuator e expor o liveness probe:

- `spring-boot-starter-actuator` no `pom.xml`;
- `management.endpoint.health.probes.enabled=true` no `application.properties`;
- documentar `GET /actuator/health/liveness` no `CLAUDE.md` (contrato da API);
- trocar a URL do cronjob de [0004](0004-reativar-keep-warm-temporario-a-cada-10-min.md)
  para `.../actuator/health/liveness`.

Plano detalhado salvo em `~/.claude/plans/n-s-n-o-vamos-fazer-agile-nebula.md`.

## Consequências

- **Bom:** Render sempre quente; Neon volta a suspender livremente e a
  consumir compute só no uso real — margem folgada na cota gratuita.
- **Bom:** o probe é um health check de verdade, útil também para o Render
  monitorar a aplicação.
- **Custo:** a primeira request real do dia ainda paga a resumção do Neon
  (< 0,5 s) — aceitável, e muito menor que o cold start do Render.
- **A decidir:** manter o endpoint de liveness público ou protegê-lo; expor ou
  não os demais endpoints do Actuator (por padrão, manter só `health`).
