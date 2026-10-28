# Step 04 — Deploy em produção: Render + Neon

## Objetivo
Colocar a API no ar de forma contínua e sem custo: o backend Spring Boot roda no **Render**
(dentro de um container Docker) e o banco PostgreSQL roda no **Neon**. Este step entrega três
coisas: o `Dockerfile`, a URL JDBC de produção com SSL e a forma de entregar as credenciais do
banco ao container (variáveis de ambiente).

## Decisões

### 1. Onde hospedar a API e o banco
| Opção | O que oferece | O que custa |
|---|---|---|
| Railway (plano inicial) | API e Postgres no mesmo painel, credenciais injetadas automaticamente | O crédito gratuito deixou de bastar para manter o serviço no ar continuamente |
| **Render (API) + Neon (Postgres)** | Dois planos gratuitos; deploy automático a cada push na `main`; banco gerenciado com SSL e backup | Dois painéis para administrar; credenciais configuradas à mão; hibernação dos dois serviços (ver step-12) |
| Plano pago em qualquer provedor | Sem hibernação | Custo recorrente em um projeto de estudo |

Venceu Render + Neon porque é a única combinação que cabe no requisito "custo zero" mantendo
deploy automático e banco gerenciado.

### 2. Como empacotar a API
O Render não tem um ambiente nativo de Java, então o caminho é entregar uma imagem Docker. Usei um
`Dockerfile` **multi-stage**: um estágio com JDK compila o projeto e outro, só com JRE, roda o
`.jar`. A alternativa seria uma imagem única com JDK, mais simples, porém maior e com ferramentas
de compilação desnecessárias em produção.

### 3. Como entregar as credenciais do banco
O Render não injeta credenciais de banco como a Railway fazia. As cinco variáveis
(`PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`) são cadastradas **manualmente** no painel
do Render com os valores do Neon. A alternativa (colocar a URL no `application-prod.properties`)
violaria a regra de nunca versionar segredos.

### 4. SSL
O Neon só aceita conexões criptografadas. Por isso a URL JDBC termina com `?sslmode=require`;
sem isso a conexão é recusada.

## ADR — Hospedar a API no Render e o Postgres no Neon
**Contexto.** Projeto privado de dois usuários, com finalidade de aprendizado e sem orçamento para
infraestrutura. O plano inicial (Railway) deixou de caber no crédito gratuito. É preciso um
container Java exposto por HTTP, um Postgres gerenciado com SSL e deploy automático.

**Decisão.** API no Render (Docker, deploy a cada push na `main`); banco no Neon. URLs de produção:
API em `https://home-budget-api-wml2.onrender.com` e frontend (Vercel) em
`https://home-budget-web-ten.vercel.app`.

**Consequências.**
- Bom: custo zero, deploy automático, banco com SSL e backup.
- Ruim: dois painéis; as credenciais vivem só nas variáveis de ambiente do Render, fora do Git.
- Risco dos planos gratuitos: o Render hiberna o serviço após ~15 min sem tráfego (a primeira
  requisição seguinte espera 30–60 s) e o Neon suspende o compute após ~5 min sem consultas e tem
  cota mensal de compute, que pausa o banco quando excedida. Isso motivou o [step-12](step-12-keep-warm-workflow.md).
- Reverter custa: trocar o banco exige migrar os dados e mudar a configuração de datasource;
  trocar a hospedagem exige só reconfigurar o deploy, pois o `Dockerfile` é portátil. O step-12
  registra que essa decisão será revista na migração para Firestore + Cloud Run.

## Como o código funciona

### `Dockerfile`
```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build   # estágio 1: JDK para compilar
WORKDIR /app
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src
RUN chmod +x mvnw && ./mvnw clean package -DskipTests   # gera target/*.jar

FROM eclipse-temurin:21-jre-alpine            # estágio 2: só o JRE, imagem menor
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar   # traz apenas o .jar do estágio 1
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```
- `AS build` dá um nome ao primeiro estágio; `COPY --from=build` busca arquivos dele. O JDK e o
  código-fonte ficam para trás e não entram na imagem final.
- `-DskipTests` pula a fase de testes do Maven durante o build da imagem.
- `EXPOSE 8080` documenta a porta em que o Spring Boot escuta.

### `application-prod.properties`
```properties
spring.datasource.url=jdbc:postgresql://${PGHOST}:${PGPORT}/${PGDATABASE}?sslmode=require
spring.datasource.username=${PGUSER}
spring.datasource.password=${PGPASSWORD}
```
`${NOME}` é substituído pelo valor da variável de ambiente de mesmo nome. Este arquivo só vale
quando o perfil `prod` está ativo (`SPRING_PROFILES_ACTIVE=prod` no Render); localmente vale o
`application.properties`, que usa o H2 em memória.

## Como verificar
Os comandos usam a produção real; apague qualquer gasto de teste ao final.

1. **Caminho feliz.** O que se verifica: a API sobe no Render e fala com o Neon.
   ```bash
   curl -i "https://home-budget-api-wml2.onrender.com/expenses?month=2026-10"
   ```
   Esperado: `200` e um array JSON. Se a API estava hibernada, a primeira resposta demora até ~1 min.
2. **Persistência.** Crie um gasto, reinicie o serviço pelo painel do Render e liste de novo.
   ```bash
   curl -i -X POST "https://home-budget-api-wml2.onrender.com/expenses" \
     -H "Content-Type: application/json" \
     -d '{"description":"Teste deploy","amount":100,"date":"2026-10-01","category":"VARIABLE"}'
   ```
   Esperado: `201`; após o reinício, o gasto continua na listagem (dados estão no Neon, não no container).
3. **Estado limite.** `GET /expenses?month=2020-01` (mês sem dados) deve devolver `200` e `[]`.
4. **Caminho de erro.** `curl -i -X DELETE ".../expenses/00000000-0000-0000-0000-000000000000"` deve
   devolver `404`. Um `500` indicaria falha de conexão com o banco (variáveis `PG*` erradas ou SSL).
5. **Regressão.** Abra `https://home-budget-web-ten.vercel.app` e confirme que lista e cria gastos.

## Conexão com o próximo passo
Com a API em produção, os próximos steps adicionam endpoints (DELETE, PUT, importação de fixos,
entrada mensal) e o [step-12](step-12-keep-warm-workflow.md) trata da hibernação dos planos gratuitos.
