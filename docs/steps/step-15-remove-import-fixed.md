# Step 15 — Remover o "importar fixos"

## Objetivo
Retirar da API o endpoint `POST /expenses/import-fixed` e tudo o que existia só para ele, **antes** de publicar
a aplicação no Cloud Run. O resto do contrato (criar, listar, editar, apagar gastos e a entrada mensal) não muda.
A categoria `FIXED`/`VARIABLE` continua existindo: ela ainda faz parte do gasto.

## Decisões

### Remover ou manter
| Opção | Vantagens | Desvantagens |
|---|---|---|
| Manter o endpoint | Nenhum trabalho; os clientes que usam o botão continuam funcionando | A funcionalidade é pouco usada; continuam existindo um endpoint, um método de service e o lote atômico (`WriteBatch`) só para ela, tudo para testar, explicar e manter |
| **Remover** | A API fica menor e mais fácil de explicar; menos código para migrar e publicar | Quebra o contrato: quem chamar o endpoint passa a receber erro |

Para duas pessoas que quase não usam o botão, o custo de manter supera o benefício.

### Por que antes do deploy
Publicar no Cloud Run um código que já se sabe que será apagado seria trabalho dobrado: o deploy, os testes na
URL nova e a documentação teriam de ser refeitos. Além disso, na virada todos os clientes (web, iOS, macOS e
Android) já vão trocar a URL da API; tirar o botão de importar na mesma atualização não cria uma segunda
atualização.

### A produção atual não é afetada
O Render faz deploy da `main`, e esta mudança está na branch `feat/firestore-persistence`. Enquanto a virada não
acontece, o endpoint continua existindo em produção. A quebra de contrato só vale a partir do Cloud Run.

### Mudança que quebra o contrato
Remover um endpoint é uma *breaking change* (mudança incompatível): por isso o commit usa `!` depois do tipo e o
rodapé `BREAKING CHANGE:`. É o sinal de que quem consome a API precisa agir; aqui, os clientes que tinham o botão.

## Como o código funciona

### O que saiu
- `ExpenseController`: o método `importFixed` e o `@PostMapping("/import-fixed")`. Sobraram as quatro operações
  de gasto: `create`, `list`, `update` e `delete`.
- `ExpenseService`: o método `importFixed`, com a regra de copiar os `FIXED` do mês anterior para o dia 1. O
  método privado `applyRequest` **ficou**, porque `create` e `update` ainda o usam.
- `ExpenseRepository`: o `saveAll` (que usava um `WriteBatch` para gravar vários documentos numa só operação
  atômica) e o import de `WriteBatch`. O `prepareForSave` **ficou**, porque o `save` o usa.
- `CLAUDE.md`: o bloco do endpoint no contrato da API. Outros repositórios importam esse arquivo, então ele
  precisa refletir o que a API realmente oferece.

### O que ficou, e por quê
`Category` continua com `FIXED` e `VARIABLE`. Elas são um dado do gasto, aparecem na API e nos clientes, e abrem
caminho para novas categorias no futuro. O que saiu foi só a *operação* que copiava os fixos.

### Regra prática que este step ilustra
Ao remover uma funcionalidade, procure o que existia **só por causa dela** (`saveAll`, `WriteBatch`) e remova
junto; e o que outros ainda usam (`applyRequest`, `prepareForSave`) fica. Uma busca por `importFixed`,
`import-fixed`, `saveAll` e `WriteBatch` depois da mudança confirma que não sobrou nada.

## Como verificar
Pré-requisitos: o emulador do Firestore rodando
(`gcloud emulators firestore start --host-port=localhost:8086`) e a API subida com
`FIRESTORE_EMULATOR_HOST=localhost:8086` e `GOOGLE_CLOUD_PROJECT=home-budget-api-arthur`. O que se verifica é que
o endpoint sumiu *e* que nada do resto quebrou.

| Camada | Chamada | Esperado |
|---|---|---|
| Compilação | `./mvnw -q clean package -DskipTests` | sem erros; nenhuma sobra de `importFixed`, `saveAll` ou `WriteBatch` |
| Caminho feliz | `POST /expenses` válido; `GET /expenses?month=2026-12` | `201`; `200` com os gastos do mês |
| Persistência | `PUT /expenses/{id}`; listar de novo | `200`, mesmo id e mesmo `createdAt`; a lista reflete a edição |
| Estado limite | `GET /income?month=...` sem registro | `200` com `amount` 0; depois do `PUT`, o valor salvo |
| Caminho de erro | `PUT`/`DELETE` com UUID inexistente; corpo inválido | `404`; `422` |
| Apagar | `DELETE /expenses/{id}` duas vezes | `204`; depois `404` |
| Endpoint removido | `POST /expenses/import-fixed?month=2026-12` | **`405`** com `Allow: PUT, DELETE` |
| Banco fora do ar | parar o emulador e `GET /expenses?month=2026-10` | `503` com `"Data store unavailable"`; log com `Data store failure` |

### Por que o endpoint removido responde `405` e não `404`
O caminho `/expenses/import-fixed` casa com o padrão `/expenses/{id}`, que existe para `PUT` e `DELETE`. O Spring
encontra o caminho, vê que o método `POST` não é aceito ali e responde `405 Method Not Allowed`, informando no
cabeçalho `Allow` os métodos permitidos. Ele confere o método **antes** de tentar converter `import-fixed` em
`UUID`; se fizesse o contrário, o resultado seria `400`. Um `405` aqui é o comportamento correto: o endpoint não
existe mais.

### O `503`, que não tinha sido verificado no step-14
Com o emulador parado, a API respondeu `503` e a mensagem fixa, e o detalhe técnico ficou só no log, como
planejado. A resposta veio em cerca de **0,3 segundo**, porque sem nenhum processo ouvindo na porta a conexão é
recusada na hora. Um Firestore lento ou inalcançável por rede (cenário diferente) pode demorar mais, pois o SDK
tenta de novo antes de desistir; esse caso **não foi medido**.

Este step foi verificado rodando a aplicação contra o emulador local, sem tocar o Firestore real nem a conta Google.

## Conexão com o próximo passo
Com o código final do contrato definido, o step-16 publica a aplicação no **Cloud Run**. Ele também troca o
`Dockerfile` (que existia por causa do Render) pelos buildpacks do Google, que detectam o projeto Maven e
fixam o Java 21 por um arquivo de configuração.
