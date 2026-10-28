# Step 12 — Keep-warm: evitar o cold start sem estourar a cota do Neon

## Objetivo
Eliminar a demora de ~1 minuto na primeira requisição depois de um período sem uso, sem gastar
a cota gratuita do banco. Este step conta o caminho completo: a primeira solução (ping a cada
5 min), o incidente que ela causou, a causa raiz e a solução atual (ping a cada 10 min).

## O problema: dois "sonos" sobrepostos
Em planos gratuitos, os dois serviços de [step-04](step-04-render-neon-deploy-migration.md) hibernam:

| Serviço | Dorme após | Custo de acordar |
|---|---|---|
| Render (API) | ~15 min sem tráfego HTTP | cold start de 30–60 s |
| Neon (Postgres) | ~5 min sem consultas (fixo no plano gratuito) | resume em ~300–500 ms |

Além disso, o Neon tem uma **cota mensal de compute**; ao excedê-la, o banco é pausado até o ciclo
seguinte.

## Decisões

### Como manter a API quente
| Opção | O que oferece | O que custa |
|---|---|---|
| Upgrade pago no Render | Sem hibernação | Custo recorrente |
| Upgrade pago no Neon | Sem suspensão nem cota apertada | Custo recorrente |
| **Ping externo periódico** | Gratuito; um GET em `/expenses?month=…` acorda API e banco, pois a rota executa uma consulta | Depende de um agendador externo; o intervalo precisa ser escolhido com cuidado (ver ADR) |

### Quem faz o ping
Primeiro um workflow agendado do GitHub Actions (`.github/workflows/keep-warm.yml`), depois também
um job no **cron-job.org**. Hoje só o cron-job.org permanece, porque dois pingers dobraram o consumo.

### Intervalo
Começou em 5 min (o mínimo do GitHub Actions) e passou a 10 min após o incidente.

## ADR — Keep-warm por ping externo (e como ele evoluiu)

### Fase 1: ping a cada 5 minutos
**Contexto.** Cold start de ~1 min incomodava o uso diário do casal.
**Decisão.** Ping de 5 em 5 min, disparado pelo GitHub Actions e por um job no cron-job.org
apontando para `/expenses?month=<mês fixo na URL>`.
**Consequências.** O cold start sumiu, mas um ping a cada 5 min impede o Neon (que suspende após
~5 min) de dormir em praticamente 100% do tempo: o compute ficou ativo ~24 h por dia. O custo não
estava visível: não havia alerta de consumo e o workflow não documentava o risco.

### Incidente: banco pausado por cota
**O que aconteceu.** No mês seguinte, todas as rotas que tocam o banco passaram a devolver `500`.
O projeto no Neon tinha consumido 110+ CU-horas em 19 dias e foi pausado automaticamente.
**Sintomas enganosos.** E-mails do Render com "deploy failed" e "Exited with status 1": o health
check falhava porque o Postgres estava pausado. Eram consequências, não bugs novos.
**Causa raiz.** Os dois pingers de 5 min mantendo o compute ativo quase 24 h por dia.
**Por que passou despercebido antes.** O primeiro mês em que o ping existiu só o teve durante a
segunda metade, e a cota não estourou.
**Observação sobre unidades.** O [step-04](step-04-render-neon-deploy-migration.md) estimava a cota
em ~190 h de compute, enquanto o incidente foi medido em CU-horas; as duas medidas não são a mesma
unidade, e o valor exato da cota deve ser conferido no painel do Neon.

### Fase 2: desligar tudo
**Decisão.** Remover o workflow do GitHub Actions e desativar o job do cron-job.org, aceitando
o cold start. Regra derivada: nenhum ping pode tocar o banco em intervalo menor que o autosuspend
do Neon (~5 min).
**Consequências.** Após o reset do ciclo gratuito, `GET /expenses` voltou a responder `200`
(confirmado por `curl`). O cold start voltou.

### Fase 3 (atual): ping a cada 10 minutos
**Contexto.** A solução ideal (um endpoint que não toque o banco) ainda não existia.
**Decisão.** Reativar **só** o cron-job.org (sem recriar o workflow do GitHub), com GET em
`https://home-budget-api-wml2.onrender.com/expenses?month=<mês corrente>`, status esperado `200`,
notificação de falha ligada, expressão `*/10 * * * *`.

**Por que 10 minutos.**
- 10 < 15: o Render **nunca** hiberna, então não há cold start.
- 10 > 5: o Neon **ainda suspende** cerca de 5 min a cada ciclo e é acordado pelo ping seguinte;
  o compute fica ativo ~50% do tempo (~90 h/mês), bem abaixo da cota.

**Consequências.** Bom: sem cold start do Render e consumo do Neon abaixo da cota por construção.
Ruim: a configuração vive fora do Git, num painel de terceiro. Se o app for aberto mais de ~5 min
depois do último ping, a primeira consulta espera o Neon retomar (< 0,5 s).
**Reverter custa:** desativar o job no cron-job.org e aceitar o cold start; nada no código muda.

### Alternativas consideradas e abandonadas
- **Liveness probe com Spring Boot Actuator** (`/actuator/health/liveness`, que responde sem tocar
  o banco): manteria só o Render quente e deixaria o Neon dormir. **Cancelada** porque o problema
  que ela resolvia (acordar o Neon a cada ping) desaparece com a migração para Firestore (não
  suspende e não tem cota de compute) e Cloud Run, onde ping de keep-warm não garante instância
  quente (o correto seria `min-instances=1`, que é pago) e o startup probe TCP padrão basta.
  Ficaram sem sentido também as decisões acessórias: expor ou não os outros endpoints do Actuator.
- **Postgres em servidor doméstico via Tailscale**: tiraria o banco do Neon e a dependência de
  cota. Exigiria Tailscale em modo userspace no container do Render e uma socket factory
  customizada no driver JDBC, e a disponibilidade passaria a depender de energia e internet de casa.
  **Cancelada**, substituída pela decisão de migrar para Firestore.
- **Monitorar o consumo de compute do Neon** e **restringir o ping ao horário de uso**: ações de
  apoio ao Neon, **canceladas** porque o Neon sai na migração.

**Estado atual.** Render, Neon e o job de 10 min seguem ativos até a migração para Firestore + Cloud
Run; quando ela acontecer, este ADR e o do step-04 serão revertidos num novo step, com link de volta.

## Como o código funciona
Este step não deixa código no repositório: o workflow do GitHub Actions foi removido e o ping vive
no cron-job.org. O que existe é o **contrato do endpoint** usado pelo ping:

```java
@GetMapping
public List<ExpenseResponse> list(@RequestParam(required = false)
        @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
    return service.listByMonth(month != null ? month : YearMonth.now());
}
```
`GET /expenses?month=yyyy-MM` executa `repository.findByDateBetweenOrderByDateDesc(...)`, uma
consulta real ao Postgres. É exatamente por isso que o ping acorda o Neon, e é a causa de a
frequência importar tanto.

O workflow removido era um `cron` agendado que fazia esse mesmo GET a cada 5 minutos.

## Como verificar
1. **Caminho feliz.** O ping está funcionando: no painel do cron-job.org, o histórico do job
   "Keep home-budget-api warm" deve mostrar execuções a cada 10 min com status `200`.
2. **Persistência (da configuração).** Confirme no painel que o job está **ativo**, com a
   expressão `*/10 * * * *` e notificação de falha ligada.
3. **Estado limite.** Não use meses sem dados: o ping continua devolvendo `200` com `[]`.
4. **Caminho de erro.** Se o Neon estiver pausado, o ping devolve `500` e o cron-job.org avisa
   por notificação; confira o painel do Neon antes de investigar o código.
5. **Regressão e efeito.**
   ```bash
   curl -s -o /dev/null -w "%{http_code} %{time_total}s\n" \
     "https://home-budget-api-wml2.onrender.com/expenses?month=2026-10"
   ```
   Esperado: `200` em poucos segundos mesmo após longa inatividade do app. No painel do Neon,
   o gráfico de compute deve mostrar períodos de suspensão entre os pings, e não uma linha contínua.

## Conexão com o próximo passo
A atualização automática da lista no frontend (polling leve e atualização ao voltar o foco da aba)
continua pendente e pertence ao repositório `home-budget-web`. O próximo grande passo é planejar a
migração para Firestore + Cloud Run, que encerra o keep-warm.
