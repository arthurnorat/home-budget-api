# Step 14 — Persistência no Firestore

## Objetivo
Fazer a API guardar e ler os dados no **Firestore**, usando o SDK Java do Google. Os endpoints, o JSON e os
códigos HTTP continuam exatamente iguais ao contrato do `CLAUDE.md`: só muda onde os dados ficam. Controllers,
DTOs, `Category` e `WebConfig` não foram alterados em comportamento. No mesmo step saem do projeto o JPA, o H2,
o driver do PostgreSQL e o teste gerado pelo Spring Initializr, que deixaram de ser usados.

## Decisões

### Biblioteca de acesso ao Firestore
| Opção | Vantagens | Desvantagens |
|---|---|---|
| **SDK nativo `google-cloud-firestore`** | Chamadas diretas e bloqueantes, no mesmo estilo dos services; pouca "mágica" para explicar | Mais código nos repositories (conversão entre objeto e documento) |
| Spring Cloud GCP (Spring Data Firestore) | Repositories como interfaces, parecido com o JPA | É reativo (Reactor, `Mono`/`Flux`), um modelo diferente do Spring MVC bloqueante usado no projeto |

### Repositories próprios com os mesmos nomes de método
Os novos `ExpenseRepository` e `MonthlyIncomeRepository` são classes comuns, mas mantêm os nomes que os services
já usavam (`save`, `findById`, `existsById`, `deleteById`, `saveAll`, `findByDateBetweenOrderByDateDesc`). Assim a
troca do banco ficou quase invisível para os services. O `MonthlyIncomeService` nem mudou. Essa é a vantagem
prática da separação em camadas.

### Como guardar as datas
| Campo | Escolha | Por quê |
|---|---|---|
| `date` (dia do gasto) | Texto ISO, `"2026-10-05"` | Um gasto pertence a um dia, não a um instante. Em texto ISO a ordem alfabética é a ordem cronológica, então dá para filtrar e ordenar. Um `Timestamp` exigiria escolher fuso horário, e um erro de fuso jogaria o gasto para o dia anterior. |
| `createdAt` (criação) | `Timestamp` do Firestore | É um instante exato, em UTC. O Firestore guarda com precisão de microssegundos, então o `Instant` é truncado a micros antes de salvar, para o valor devolvido na criação ser igual ao lido depois. |

### Consulta do `import-fixed`
| Opção | Vantagens | Desvantagens |
|---|---|---|
| **Buscar os gastos do mês anterior e filtrar `FIXED` em memória** | Sem índice composto; emulador e produção se comportam igual | Lê também os gastos variáveis do mês (poucas dezenas, irrelevante) |
| Filtrar categoria e data no próprio Firestore | Lê só o necessário | Exige criar um índice composto na nuvem. O emulador não exige índices, então um erro só apareceria em produção |

### Erros do banco
As chamadas do SDK podem falhar com `ExecutionException` ou `InterruptedException`. Um único método,
`FirestoreSupport.await`, transforma essas falhas numa exceção de domínio, `PersistenceException`, que o
`GlobalExceptionHandler` responde com `503` e uma mensagem fixa. O detalhe técnico vai só para o log, para não
expor informação interna ao cliente.

### Desenvolvimento local
O banco em memória deu lugar ao **emulador do Firestore**, que roda num processo separado. Ele é gratuito, não
toca no banco real e também perde os dados ao parar. A alternativa seria um projeto Google só para
desenvolvimento, que usaria a cota gratuita e exigiria login.

### Branch
Todo o step foi feito na branch `feat/firestore-persistence`, para a `main` não receber um código que depende de
uma infraestrutura ainda não publicada.

## ADR — Modelo de documentos no Firestore

**Contexto.** O Firestore guarda coleções de documentos, sem tabelas, sem JOIN e com consultas limitadas ao que os
índices permitem. O contrato da API exige UUID como id do gasto e uma única entrada por mês.

**Decisão.**
- Coleção `expenses`: o id do documento é o UUID do gasto, gerado pela aplicação. Campos `description`, `amount`
  (centavos), `date` (texto ISO), `category` (`FIXED`/`VARIABLE`) e `createdAt` (`Timestamp`).
- Coleção `monthly_income`: o id do documento é o mês (`yyyy-MM`). Campo `amount` (centavos).
- Só consultas que não precisam de índice composto: listar o mês filtra e ordena pelo mesmo campo (`date`).

**Consequências.**
- Bom: buscar, atualizar e apagar por id vão direto ao documento; um mês nunca tem duas entradas, por construção.
- Ruim: regras que precisem combinar vários campos numa consulta vão exigir índices compostos ou filtro em memória;
  não existem as transações SQL de antes (o `import-fixed` usa um lote atômico, que basta aqui); um documento
  editado à mão sem algum campo quebra a leitura dele.
- Reverter custa: reescrever os dois repositories e migrar os dados para outro modelo.

## Como o código funciona

### `pom.xml`
O `libraries-bom` do Google entra em `<dependencyManagement>`. Um **BOM** (Bill of Materials) é um catálogo de
versões compatíveis entre si; com ele, a dependência `google-cloud-firestore` fica sem versão e herda a certa.
Saíram `spring-boot-starter-data-jpa`, `h2`, `postgresql` e `spring-boot-starter-test`.

### `FirestoreConfig`
```java
@Bean
public Firestore firestore() {
    return FirestoreOptions.getDefaultInstance().getService();
}
```
`@Configuration` marca uma classe que fabrica objetos para o Spring; cada método `@Bean` produz um objeto que o
Spring entrega a quem o pedir no construtor (aqui, os repositories). A configuração vem do ambiente:
`GOOGLE_CLOUD_PROJECT` define o projeto; no Cloud Run as credenciais vêm da conta de serviço
(**Application Default Credentials**); com `FIRESTORE_EMULATOR_HOST`, o cliente usa o emulador e não pede credencial.

### `FirestoreSupport.await`
```java
try {
    return future.get();
} catch (ExecutionException e) {
    throw new PersistenceException("Firestore failed to " + action, e);
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    throw new PersistenceException("Interrupted while trying to " + action, e);
}
```
O SDK devolve um `ApiFuture`, uma promessa de resultado. O `.get()` espera a resposta. Em caso de
`InterruptedException`, o código restaura o aviso de interrupção da thread antes de lançar o erro, em vez de
engoli-lo.

### `ExpenseRepository`
- `save`: na primeira gravação gera o UUID e o `createdAt`; depois grava o documento com `set`, que cria ou
  substitui.
- `saveAll`: usa um `WriteBatch`, que grava todos os documentos numa operação **atômica** (todos ou nenhum; até
  500 por lote).
- `findByDateBetweenOrderByDateDesc`:
  ```java
  collection()
      .whereGreaterThanOrEqualTo("date", start.toString())
      .whereLessThanOrEqualTo("date", end.toString())
      .orderBy("date", Query.Direction.DESCENDING)
      .get()
  ```
- `toDocument` e `fromSnapshot` convertem a entidade num `Map` de campos e o documento lido de volta em `Expense`.

### `ExpenseService`
`create` e `update` usam o método privado `applyRequest`, que copia os quatro campos do request para a entidade.
O `importFixed` pega os gastos do mês anterior, filtra `Category.FIXED` e grava as cópias com data no dia 1 do mês
pedido.

### `MonthlyIncomeRepository`
O mês é o caminho do documento (`monthly_income/2026-10`). `findById` lê o documento; `save` grava
`{"amount": ...}` com `set`. O service continua devolvendo `amount` 0 quando o documento não existe.

### Entidades, exceções e limpeza
- `Expense` e `MonthlyIncome` viraram classes simples, sem anotações. A validação fica só nos DTOs (`@Valid` no
  controller), que rejeitam a entrada antes de chegar ao banco.
- `ExpenseNotFoundException` foi para o pacote `expense`, porque só pertence a essa feature; o pacote `exception`
  ficou com o que é compartilhado (`GlobalExceptionHandler` e `PersistenceException`).
- `application.properties` ficou só com o nome da aplicação; o `application-prod.properties` e o teste gerado pelo
  Initializr foram apagados; saíram os comentários que só explicavam Java ou Spring.

## Como verificar
Pré-requisitos: o emulador rodando (`gcloud emulators firestore start --host-port=localhost:8086`) e a API subida com
as variáveis `FIRESTORE_EMULATOR_HOST=localhost:8086` e `GOOGLE_CLOUD_PROJECT=home-budget-api-arthur`.

| Caso | Chamada | Esperado |
|---|---|---|
| Criar | `POST /expenses` válido | `201`, UUID novo, `createdAt` com microssegundos |
| Listar o mês | `GET /expenses?month=2026-10` | `200`, só gastos do mês, data decrescente; dia 1 do mês seguinte fica de fora |
| Sem `month` | `GET /expenses` | lista o mês atual |
| Atualizar | `PUT /expenses/{id}` | `200`, mesmo id e mesmo `createdAt`, sem duplicata |
| Entrada inválida | descrição vazia ou valor negativo | `422` com os campos em `errors` |
| Id inexistente | `PUT` ou `DELETE` | `404` |
| Apagar | `DELETE /expenses/{id}` | `204`; repetido, `404` |
| Importar fixos | `POST /expenses/import-fixed?month=2026-12` | `201`, só `FIXED`, data `2026-12-01`, ids novos |
| Virada de ano | `import-fixed?month=2027-01` | busca em dezembro de 2026 |
| Mês anterior vazio | `import-fixed` | `201` e `[]` |
| Entrada mensal | `GET /income` sem registro; `PUT` duas vezes | `amount` 0; depois o último valor, sem duplicar |
| Entrada inválida | `amount` negativo; `month=outubro` | `422`; `400` |
| Persistência | reiniciar a API | dados mantidos (ficam no emulador); parar o emulador apaga tudo |

Não verificado neste step: a resposta `503` com o Firestore fora do ar, e a construção da imagem Docker (feita na
nuvem no step-15).

## Conexão com o próximo passo
Com a API funcionando sobre o Firestore, o step-15 publica a aplicação no **Cloud Run**, usando a conta de serviço
`home-budget-api-run` e o banco real em São Paulo.
