# 0004 — Reativar keep-warm temporário a cada 10 min

- **Status:** Aceito (2026-09-08)
- **Substitui:** [0003](0003-remover-keep-warm-apos-incidente-de-cota.md)
- **Será substituído por:** [0005](0005-liveness-probe-com-actuator-para-keep-warm-seguro.md), quando o liveness probe existir
- **Relacionado:** [0001](0001-hospedagem-no-render-e-neon.md), [0002](0002-keep-warm-via-ping-externo.md)

## Contexto

Depois de [0003](0003-remover-keep-warm-apos-incidente-de-cota.md), a produção
está sem keep-warm e voltou a ter cold start de ~1 min na primeira request após
inatividade. Para o uso diário do casal, isso é ruim o suficiente para querer
resolver já.

A solução definitiva — um endpoint de liveness que não toca o banco
([0005](0005-liveness-probe-com-actuator-para-keep-warm-seguro.md)) — ainda não
foi implementada. Enquanto isso, o único endpoint disponível para o ping é
`/expenses`, que executa uma query.

O erro de [0002](0002-keep-warm-via-ping-externo.md) foi o **intervalo de 5
min**, que mantinha o Neon acordado 24/7. A lição de
[0003](0003-remover-keep-warm-apos-incidente-de-cota.md) dá a saída: usar um
intervalo **maior** que o autosuspend do Neon.

Limites de suspensão (do [0001](0001-hospedagem-no-render-e-neon.md)):

| Serviço | Suspende após | Ao acordar |
|---------|---------------|------------|
| Render  | ~15 min sem tráfego HTTP | cold start ~30–60 s |
| Neon    | ~5 min sem query (fixo no free) | resume ~300–500 ms |

## Decisão

Vamos reativar **apenas o cronjob do cron-job.org** (sem recriar o workflow do
GitHub Actions — dois pingers foi o que agravou o incidente), com:

- **URL:** `https://home-budget-api-wml2.onrender.com/expenses?month=<mês corrente>`
- **Método:** GET · **status esperado:** 200 · **notificação de falha:** ligada
- **Intervalo:** **10 minutos** (`*/10 * * * *`)

Por que 10 min:

- **10 < 15** → o Render **nunca** hiberna: fica sempre quente, sem cold start.
- **10 > 5** → o Neon **ainda suspende ~5 min a cada ciclo** e é acordado pelo
  ping seguinte. O compute fica ativo ~50% do tempo (~90 h/mês), bem abaixo da
  cota gratuita (~190 h/mês). É isso que impede o incidente de se repetir.

Trade-off aceito: se o app for aberto mais de ~5 min depois do último ping, a
primeira query espera a resumção do Neon (< 0,5 s) — imperceptível na prática.

Opcional (não adotado agora): restringir o cron ao horário de uso
(ex.: 06:00–01:00) para folga extra na cota.

O parâmetro `month=` só precisa estar no formato `yyyy-MM`; a query roda de
qualquer forma. Atualizar para o mês corrente de tempos em tempos é só higiene.

## Consequências

- **Bom:** cold start do Render eliminado de novo; consumo do Neon dentro da
  cota por construção (intervalo > autosuspend).
- **Ruim:** configuração operacional volta a viver **fora do repositório**, num
  painel de terceiro (cron-job.org). Está listada em `docs/operations.md` e é
  uma decisão explicitamente temporária.
- **Provisório:** assim que [0005](0005-liveness-probe-com-actuator-para-keep-warm-seguro.md)
  entrar, a URL do ping muda para o endpoint de liveness e o Neon passa a
  dormir de verdade, acordando só no uso real. Este ADR passa a
  `Substituído por 0005`.
