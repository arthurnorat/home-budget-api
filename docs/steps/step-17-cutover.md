# Step 17 — Virada: os clientes passam a usar o Cloud Run

## Objetivo
Fazer os quatro clientes (web, Android, iOS e macOS) falarem com a API do Cloud Run, que grava no Firestore, e
mergear a branch `feat/firestore-persistence` na `main`. Até aqui a API nova já estava no ar
([step-16](step-16-cloud-run-deploy.md)), mas ninguém a usava: os clientes ainda apontavam para o Render. Este step é a
troca de endereço, mais as decisões sobre o que acontece com a infraestrutura antiga.

Os dados antigos do Neon **não** foram migrados: o Firestore começou vazio (ver o ADR abaixo).

## Decisões

### Em que ordem virar
A ordem planejada foi: testar os clientes contra o Firestore real, suspender o deploy automático do Render, mergear
a branch na `main` e publicar os clientes. Na prática houve uma inversão inofensiva: o web foi enviado ao GitHub
**antes** do merge da API, e como o Vercel publica a cada push na `main` do web, o site em produção já passou a usar
o Cloud Run. Não houve dano, porque a API nova já estava no ar, publicada a partir da branch. A lição é que, no
web, **enviar o código é publicar**; já nos apps nativos, enviar não publica nada, porque eles só chegam ao
aparelho por um build.

### Por que travar o deploy do Render antes do merge
O Render faz deploy automático da `main` da API. Depois do merge, a `main` tem o código do Firestore, que não tem
as variáveis do Postgres; se o Render o publicasse, a API antiga cairia. Desligar só o **Auto-Deploy** (sem suspender
nem apagar o serviço) mantém o Render no ar com o código antigo, como plano B, sem risco de publicar sozinho.

### O keep-warm
O job do cron-job.org pingava o Render a cada 10 minutos ([step-12](step-12-keep-warm-workflow.md)). Com os clientes
fora do Render, esse ping só consumia a cota do Neon, então o job foi **desativado** (e não apagado, para poder
ser religado num rollback). Ele **não** foi reapontado para o Cloud Run: o cold start do Cloud Run, de cerca de 10
segundos, continua aceito, como decidido no step-16. Repontar o ping ou usar `min-instances=1` ficam para
reavaliar com o uso real.

### Render e Neon continuam existindo
Render e Neon **não foram apagados**. Eles servem de plano B: se algo grave aparecer nos clientes novos, o rollback
é voltar a URL antiga e publicar de novo. Só serão apagados depois de uma janela sem problemas e da decisão sobre o
fim do período de teste gratuito do Google Cloud, tratada à parte.

## ADR — Começar o Firestore vazio, sem migrar o histórico do Neon
**Contexto.** A API foi trocada de PostgreSQL (Neon) para Firestore, que tem outro modelo de dados. Os usuários são
dois, o app lista um mês por vez e o histórico não é consultado. Migrar exigiria um script de uso único e uma
verificação cruzada entre os dois bancos.

**Decisão.** Não migrar. O Firestore começa vazio e os gastos fixos do mês corrente são lançados de novo à mão.

**Consequências.**
- Bom: nenhuma ferramenta de migração para manter; a virada fica uma troca de URL.
- Ruim: o histórico antigo deixa de aparecer nos clientes. Ele continua no Neon **enquanto o Neon existir**, e depois
  disso se perde.
- **Reverter custa:** enquanto o Neon existir, dá para escrever o script e importar depois (preservando `expenseId`
  e `createdAt`). Depois que o Neon for apagado, não há como recuperar.

## Como o código funciona
O contrato da API não mudou; mudou o endereço que cada cliente chama e, em dois deles, um recurso foi removido.

| Cliente | Arquivo | O que mudou |
|---|---|---|
| Web (Angular) | `src/environments/environment.prod.ts` | `apiUrl` e `incomeUrl` apontam para o Cloud Run. O `environment.ts` de desenvolvimento continua em `localhost:8080` |
| Web | `app.ts`, `app.html`, `expense-table.*`, `expense.service.ts` | O botão "Importar" e todo o código de `importFixed` saíram, junto com o CSS `.btn-import` |
| Android (Kotlin) | `network/RetrofitProvider.kt` | A constante `BASE_URL` aponta para o Cloud Run. O comentário sobre o cold start passou a falar do Cloud Run |
| iOS (Swift) | `Services/ExpenseService.swift` | A URL do ramo Release de `defaultBaseURL` |
| macOS (Swift) | `Services/ExpenseService.swift`, `Services/IncomeService.swift` | A URL do ramo Release; o método `importFixed(month:)` foi removido (ninguém o chamava) |

### `#if DEBUG` nos apps Swift
```swift
private static var defaultBaseURL: URL {
    #if DEBUG
    URL(string: "http://localhost:8080")!
    #else
    URL(string: "https://home-budget-api-689350097433.southamerica-east1.run.app")!
    #endif
}
```
`#if DEBUG` é uma **compilação condicional**: o compilador inclui um dos dois ramos no binário, conforme a
configuração do build. Em `Debug` (o padrão ao rodar pelo Xcode) o app chama `localhost:8080`; em `Release`, o Cloud
Run. **Só o ramo Release mudou.** Por isso, para testar contra o Firestore real, é preciso rodar em Release.

### O Android não separa debug e release
`RetrofitProvider.kt` tem uma única constante `BASE_URL`, sem separar `debug` de `release`. Todo build do app,
inclusive o de desenvolvimento, chama o Cloud Run. Isso é mais simples, mas significa que **testar o app grava dados
reais**.

## Riscos e dívida técnica
- **A API é pública:** qualquer pessoa com a URL pode ler e alterar os dados. A autenticação é a Fase 3.
- **O web não trata erro de rede:** quando a API está fora do alcance, a tela fica em "Carregando..." para sempre,
  sem mensagem de erro. Pendência do repositório do web, não alterada aqui.
- **O macOS ainda não tem telas:** só a URL e a remoção do método foram feitas; falta testar quando houver interface.
- **Render, Neon e o job do cron-job.org ainda existem** (o job desativado). Precisam ser apagados depois, e só depois
  da janela de rollback e da decisão sobre o período de teste do Google Cloud.
- **Cold start de cerca de 10 segundos** no primeiro acesso depois de ociosidade.
- O Android grava dados reais mesmo em builds de desenvolvimento.

## Conexão com o próximo passo
Com os clientes no Cloud Run, o step-18 atualiza a documentação que ainda descreve o Render e o Neon: o `CLAUDE.md`
do projeto (em inglês), o README e a correção de steps antigos. Depois, a Fase 3 começa no step-19 com a
autenticação, que fecha o maior risco listado acima.
