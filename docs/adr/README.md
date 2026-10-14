# Architecture Decision Records (ADR)

Registro das decisões de arquitetura e de infraestrutura deste backend — em
especial as que **não aparecem no código**: escolha de provedores de
hospedagem, configurações feitas à mão em painéis de terceiros, jobs externos,
e assim por diante.

## Por que existe

O `git log` conta o que mudou no código. Ele não conta:

- por que o banco está no Neon e não em outro lugar;
- que existe (ou existiu) um cron externo batendo em produção;
- que uma configuração foi ligada, causou um problema e foi revertida.

Sem esse registro, um novo integrante do time — ou você mesmo daqui a seis
meses — não tem como saber dessas decisões nem do histórico delas. Cada ADR
é um arquivo curto e versionado que preenche essa lacuna.

## Formato

Usamos o formato de Michael Nygard, com cinco seções:

- **Título** — `NNNN — frase curta no imperativo ou substantiva`
- **Status** — `Proposto` · `Aceito` · `Substituído por NNNN` · `Descontinuado`
- **Contexto** — o problema e as forças em jogo, sem a decisão ainda
- **Decisão** — o que foi decidido, em voz ativa ("Vamos...")
- **Consequências** — o que passa a ser verdade depois disso: o bom, o ruim e o arriscado

## Regras

1. **Numeração sequencial** com quatro dígitos (`0001`, `0002`, ...). Nunca reutilizar um número.
2. **ADR não se apaga e não se reescreve.** Quando uma decisão deixa de valer,
   cria-se um novo ADR que a substitui e edita-se **apenas o campo `Status`** do
   antigo para `Substituído por NNNN`. Assim a linha do tempo fica preservada.
3. Um ADR por decisão. Se a decisão for revertida, isso é uma *nova* decisão e
   ganha o próprio ADR.
4. Data no formato ISO (`AAAA-MM-DD`) em cada mudança de status.

## Como adicionar um

1. Copie a estrutura de qualquer ADR existente.
2. Use o próximo número livre.
3. Comece com `Status: Proposto` se ainda estiver em discussão, ou `Aceito` se já foi decidido.
4. Adicione uma linha no índice abaixo.

## Índice

| ADR | Título | Status |
|-----|--------|--------|
| [0001](0001-hospedagem-no-render-e-neon.md) | Hospedar a API no Render e o Postgres no Neon | Aceito |
| [0002](0002-keep-warm-via-ping-externo.md) | Keep-warm por ping externo a cada 5 min | Substituído por 0003 |
| [0003](0003-remover-keep-warm-apos-incidente-de-cota.md) | Remover o keep-warm após esgotar a cota do Neon | Substituído por 0004 |
| [0004](0004-reativar-keep-warm-temporario-a-cada-10-min.md) | Reativar keep-warm temporário a cada 10 min | Aceito |
| [0005](0005-liveness-probe-com-actuator-para-keep-warm-seguro.md) | Liveness probe com Actuator para keep-warm seguro | Proposto |
| [0006](0006-self-host-do-postgres-em-servidor-domestico.md) | Mover o Postgres para um servidor doméstico via Tailscale | Proposto |
