# Step 16 — Deploy no Cloud Run pelos buildpacks

## Objetivo
Publicar a API no **Cloud Run**, lendo e gravando no Firestore real, sem escrever um `Dockerfile`: o Google gera a
imagem de contêiner a partir do código. O contrato da API continua o mesmo do `CLAUDE.md`; o que muda é onde ela
roda. A URL do serviço é `https://home-budget-api-689350097433.southamerica-east1.run.app`.

## Decisões

### Como gerar a imagem de contêiner
| Opção | Vantagens | Desvantagens |
|---|---|---|
| Manter o `Dockerfile` | Controle total do que entra na imagem; funciona em qualquer hospedagem | Mais um arquivo para manter e explicar; ele só existia por causa do Render |
| **Buildpacks do Google** (`--source .`) | Sem `Dockerfile`; o Cloud Build detecta o projeto Maven, compila e monta a imagem; atualizações de segurança da imagem base vêm do Google | Menos controle: o formato da imagem fica por conta dos buildpacks; build mais lento que usar uma imagem pronta |

Os buildpacks vencem aqui porque o projeto é um Spring Boot comum e não precisa de nada fora do padrão. O
`Dockerfile` foi removido do repositório.

### Qual versão do Java
Sem instrução, os buildpacks usam a versão suportada **mais recente**, que pode mudar sem aviso. Por isso o
arquivo `project.toml` na raiz fixa `GOOGLE_RUNTIME_VERSION = "21"`, a mesma versão do `pom.xml`. O log de
inicialização confirmou: `Starting HomeBudgetApplication ... using Java 21.0.12.1`.

### Como publicar
| Opção | Vantagens | Desvantagens |
|---|---|---|
| **Comando manual** (`gcloud run deploy`) | Simples; cada publicação é uma decisão consciente; nada roda sem eu pedir | Esquecer de publicar deixa a nuvem defasada |
| Deploy contínuo ligado ao GitHub | Publica a cada push | Mais peças para configurar e explicar; um push errado vai para produção |

### Configuração do serviço
| Ajuste | Valor | Por quê |
|---|---|---|
| Região | `southamerica-east1` | Mesma do Firestore: menor latência |
| Conta de serviço | `home-budget-api-run` | Só tem `roles/datastore.user`. A conta padrão do Compute tem papel Editor, amplo demais |
| Acesso público | permitido | Os clientes (web e apps) chamam a API sem login. **Isso significa que qualquer pessoa com a URL pode ler e alterar os dados**; a autenticação fica para a Fase 3 |
| Memória | 512 MiB | Suficiente para subir e passar no teste do contrato; carga real não foi medida |
| `JAVA_TOOL_OPTIONS` | `-XX:MaxRAMPercentage=75` | Faz a JVM usar até 75% da memória do contêiner, em vez do padrão menor |
| `GOOGLE_CLOUD_PROJECT` | id do projeto | O SDK do Firestore descobre o projeto por aqui |
| Startup CPU boost | ligado | Mais CPU durante a subida da JVM: reduz o cold start |
| `min-instances` | 0 | Sem instância ligada, sem custo parado |
| `max-instances` | 1 | Teto de custo e de concorrência: um orçamento do Google só avisa, não bloqueia gastos |

### Sem keep-warm, com cold start aceito
No Render, havia um ping a cada 10 minutos para a API não hibernar. Aqui isso não existe: com `min-instances=0`,
a instância desliga quando ninguém usa. A próxima chamada paga o **cold start**. Medido nos logs do deploy:
`Started HomeBudgetApplication in 6.874 seconds`, e cerca de **10 segundos** desde o Cloud Run iniciar a
instância até a aplicação estar pronta. Chamadas seguintes, com a instância ligada, responderam em menos de 2
segundos (o primeiro `curl` levou 1,7 s). Manter `min-instances=1` eliminaria a espera, mas **tem custo** mesmo
sem tráfego. Para duas pessoas, 10 segundos de vez em quando é o preço aceito pela gratuidade.

### Por que a API não usa listeners em tempo real nem cache offline
O Firestore tem recursos famosos, como ouvir mudanças em tempo real e guardar dados em cache para uso offline. Eles
pertencem aos **SDKs de cliente** (web e mobile), que falam direto com o banco. Aqui os clientes falam com a
**nossa API**, que usa o SDK de **servidor**: ele só faz leituras e gravações pontuais, uma por requisição. Não
existe, no servidor, uma tela para atualizar nem um modo offline. Por isso o código simplesmente lê e grava, e o
`FirestoreSupport.await` espera o resultado de cada operação.

### Limpeza de imagens antigas
Cada deploy guarda uma nova imagem no **Artifact Registry**, repositório `cloud-run-source-deploy`. Cada imagem
tem cerca de 291 MB, e a cota gratuita de armazenamento é de 0,5 GB. Duas políticas de limpeza, que trabalham
juntas, controlam isso:

| Política | Efeito |
|---|---|
| `keep-recent-4` (manter) | As 4 versões mais recentes nunca são apagadas, ficando o deploy atual e o anterior para voltar atrás |
| `delete-older-than-1d` (exclusão condicional) | Apaga o que tiver mais de 1 dia |

Quando uma versão cai nas duas, a regra de **manter** vence. A verificação de vulnerabilidades do repositório fica
desligada, porque é paga por imagem.

## Como o código funciona

### `project.toml`
```toml
[[build.env]]
name = "GOOGLE_RUNTIME_VERSION"
value = "21"
```
É um arquivo de configuração lido pelos buildpacks. `[[build.env]]` declara uma variável de ambiente que existe
**só durante o build**, e `GOOGLE_RUNTIME_VERSION` diz qual versão do Java usar.

### O que os buildpacks fazem no deploy
1. O comando envia o código para o Cloud Build (o `.gitignore` é respeitado, então `target/` não vai).
2. O Cloud Build reconhece o projeto Maven pelo `pom.xml`, usa o `mvnw` do projeto e gera o `.jar`.
3. Monta uma imagem com o Java 21 e o `.jar`, e a guarda no Artifact Registry.
4. O Cloud Run cria uma **revisão** (`home-budget-api-00001-t7w`) com essa imagem e manda 100% do tráfego para ela.

O código da API não mudou neste step: o Cloud Run espera, por padrão, que o contêiner escute na porta 8080, que
é também a porta padrão do Spring Boot (o log confirma `Tomcat started on port 8080`).

## Como verificar
Verificado na URL do Cloud Run, com o Firestore real. Os dados de teste usaram datas de 2099 e foram apagados no
fim (a entrada mensal de teste, que a API não consegue apagar, foi removida pelo console do Firestore).

| Camada | Caso | Resultado |
|---|---|---|
| Caminho feliz | `POST /expenses` duas vezes; `GET /expenses?month=2099-01` | `201`; `200` só com os gastos do mês, data decrescente; o do dia 1 do mês seguinte fica de fora |
| Persistência | `PUT /expenses/{id}` e nova listagem | `200`, mesmo id e mesmo `createdAt` |
| Estado limite | `GET /expenses` sem `month`; `GET /income` sem registro | `200` com `[]`; `200` com `amount` 0 |
| Entrada mensal | `PUT /income` duas vezes | `200`, o último valor, sem duplicar |
| Caminho de erro | `PUT`/`DELETE` com UUID inexistente; corpo inválido; `month=outubro`; `amount` negativo | `404`; `422`; `400`; `422` |
| Regressão | `POST /expenses/import-fixed` | `405` com `Allow: PUT, DELETE` |
| CORS | `OPTIONS` com a origem do Vercel; com outra origem | `200` com `access-control-allow-origin`; `403` |
| Limpeza | `DELETE` dos gastos de teste; listas vazias | `204`; `[]` |

Nos logs do serviço: Java 21.0.12.1, `Picked up JAVA_TOOL_OPTIONS: -XX:MaxRAMPercentage=75` (única linha, sem opções
extras dos buildpacks) e nenhum aviso ou erro.

Abrir a URL raiz no navegador mostra a página "Whitelabel Error Page" com `404`: a API não tem rota em `/`. Os
endpoints começam em `/expenses` e `/income`.

**Não verificado:**
- A resposta `503` com o Firestore fora do ar, no Cloud Run (só no emulador, no [step-15](step-15-remove-import-fixed.md)).
- O comportamento sob carga e se 512 MiB bastam nela.
- A cota gratuita do Cloud Run em `southamerica-east1`, que não foi confirmada: olhar o relatório de faturamento
  nas primeiras semanas.
- A efetividade das políticas de limpeza: elas só agem depois de passar de 4 versões e de 1 dia.

## Conexão com o próximo passo
O Firestore real está vazio e os dados de verdade continuam no Neon. O step-17 leva esses dados
para o Firestore, preservando `expenseId` e `createdAt`. Depois, o step-18 troca a URL nos quatro clientes e desliga
o Render e o Neon.
