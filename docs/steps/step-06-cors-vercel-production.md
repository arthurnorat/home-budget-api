# Step 06 — CORS para o frontend em produção (Vercel)

## Objetivo
Fazer o frontend Angular publicado no Vercel conseguir chamar a API. O navegador só deixa uma página
conversar com outra origem se a API autorizar essa origem explicitamente. Este step troca, no `WebConfig`,
a URL provisória do Vercel pela URL real de produção: `https://home-budget-web-ten.vercel.app`.

## Decisões

### Como autorizar a origem
| Opção | O que oferece | O que custa |
|---|---|---|
| `allowedOrigins("*")` | Nunca mais mexer | Qualquer site, de qualquer pessoa, pode chamar a API pelo navegador de um usuário |
| **Lista explícita de origens** | Só o frontend conhecido consegue | Cada novo cliente web exige editar o `WebConfig` e fazer deploy |
| Origem por variável de ambiente | Muda sem recompilar | Mais uma configuração fora do Git para administrar |

Escolhi a lista explícita: são só duas origens (`localhost:4200` e o Vercel), e a API não tem
autenticação ([step-01](step-01-spring-boot-initial-setup.md)), então restringir quem chama pelo navegador é uma
camada de proteção barata.

## Como o código funciona
```java
.allowedOrigins(
    "http://localhost:4200",                 // Angular local
    "https://home-budget-web-ten.vercel.app" // Vercel
)
```
- A origem é comparada **exatamente**: protocolo, domínio e porta. `http://` não é `https://`, e uma
  barra no final da URL a invalida.
- A URL provisória escolhida no [step-03](step-03-postgres-cors-exception-handling.md) tinha sido um palpite;
  o domínio real só existiu depois da primeira publicação no Vercel.
- O CORS é checado pelo navegador. `curl` e apps nativos ignoram essa regra; ela não substitui autenticação.
- Mudar a lista exige novo deploy no Render, que acontece automaticamente a cada push na `main`
  ([step-04](step-04-render-neon-deploy-migration.md)).

## Como verificar
1. **Caminho feliz.** Abra `https://home-budget-web-ten.vercel.app`: a lista de gastos carrega. No console
   do navegador (F12) não aparecem erros de CORS.
2. **Persistência.** Crie um gasto no site e recarregue a página; ele continua lá.
3. **Estados limite.** Mês sem gastos: a tela mostra a lista vazia, sem erro.
4. **Caminho de erro.** Simule uma origem não autorizada:
   ```bash
   curl -i "https://home-budget-api-wml2.onrender.com/expenses?month=2026-10" -H "Origin: https://exemplo.com"
   ```
   Esperado: `403 Invalid CORS request`. Com `-H "Origin: https://home-budget-web-ten.vercel.app"`: `200` e
   o cabeçalho `Access-Control-Allow-Origin`.
5. **Regressão.** `localhost:4200` continua autorizado (rode o frontend local, se quiser conferir).

## Conexão com o próximo passo
Com o frontend publicado e conversando com a API, as próximas mudanças são funcionalidades: renomear o
projeto ([step-07](step-07-project-rename.md)) e depois editar gastos ([step-08](step-08-put-expense-editing.md)).
