# 0002 — Keep-warm por ping externo a cada 5 min

- **Status:** Substituído por [0003](0003-remover-keep-warm-apos-incidente-de-cota.md) (2026-08-20)
- **Relacionado:** [0001](0001-hospedagem-no-render-e-neon.md)

## Contexto

Consequência direta do [0001](0001-hospedagem-no-render-e-neon.md): depois de
alguns minutos sem uso, a primeira request do dia demorava ~1 min por causa de
dois "sonos" independentes e sobrepostos:

- Render (free) hiberna o serviço web após ~15 min sem tráfego HTTP;
- Neon (free) suspende o compute do banco após ~5 min sem query.

Opções avaliadas:

1. upgrade pago no Render — descartado (custo);
2. upgrade pago no Neon — descartado (custo);
3. ping externo periódico mantendo os dois acordados — escolhido, por ser gratuito.

## Decisão

Vamos manter a API e o banco sempre quentes com **ping periódico a cada 5
minutos** em `GET /expenses?month=<mês>`. Como esse endpoint executa uma query
real, um único ping acorda tanto o Render quanto o Neon.

Cinco minutos foi escolhido por ser o menor intervalo do agendador do GitHub
Actions, por manter a API sempre abaixo do limite de 15 min do Render e por
minimizar a janela em que o Neon pode suspender (~5 min).

Dois disparadores acabaram coexistindo:

- `.github/workflows/keep-warm.yml` — workflow agendado (commit `d4fc2e6`, 2026-07-16);
- um cronjob externo no **cron-job.org** ("Keep home-budget-api warm"),
  apontando para `/expenses?month=2026-07` (mês fixo na URL).

## Consequências

- **Bom:** cold start eliminado; API e banco respondem na hora a qualquer momento.
- **Ruim / risco não percebido na época:** um ping a cada 5 min impede o Neon
  de suspender **em praticamente 100% do tempo**, mantendo o compute ativo
  ~24 h/dia. Isso consome a cota mensal de compute do plano gratuito muito
  antes do fim do mês. Julho não deu problema só porque o workflow rodou
  apenas os últimos ~15 dias do mês.
- O `keep-warm.yml` não deixava claro esse custo, e não havia alerta de
  consumo do Neon — a falha só apareceu pelo sintoma (500 em produção).
- Levou ao incidente descrito em [0003](0003-remover-keep-warm-apos-incidente-de-cota.md).
