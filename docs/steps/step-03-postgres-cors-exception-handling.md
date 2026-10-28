# Step 03 — Postgres em produção, CORS e tratamento de erros de validação

## Objetivo
Preparar a API para conversar com o mundo real: separar a configuração de desenvolvimento (H2) da de
produção (Postgres), liberar o navegador do frontend Angular a chamar a API (CORS) e devolver erros de
validação num formato claro (`422` com a lista de campos inválidos).

## Decisões

### Um banco por perfil
| Opção | O que oferece | O que custa |
|---|---|---|
| Só H2 em todos os ambientes | Zero configuração | Dados somem a cada reinício: inviável em produção |
| Só Postgres em todos os ambientes | Ambiente igual ao de produção | Obriga a instalar e manter um Postgres local |
| **H2 localmente e Postgres em produção, por perfil Spring** | Desenvolvimento sem instalação; produção durável | Pequenas diferenças entre os bancos podem esconder bugs |

O perfil `prod` (ativado por `SPRING_PROFILES_ACTIVE=prod`) carrega o `application-prod.properties` por
cima do `application.properties`.

### `ddl-auto` diferente em cada perfil
Localmente usa-se `create-drop` (cria e apaga as tabelas). Em produção, `update`: o Hibernate cria o que
falta, mas **nunca apaga dados** e também não altera colunas já existentes. É simples, mas não substitui
migrações versionadas.

### Credenciais por variáveis separadas
A configuração inicial usava uma única `DATABASE_URL`. Passou a montar a URL com variáveis individuais
(`PGHOST`, `PGPORT`, `PGDATABASE`), mais `PGUSER` e `PGPASSWORD`. Assim cada parte vem do ambiente e nada
sensível entra no Git.

### Driver explícito
Com H2 e Postgres no classpath, o Spring Boot pode escolher o driver errado. Por isso o perfil `prod`
declara `spring.datasource.driver-class-name=org.postgresql.Driver`. (Isso foi corrigido depois de a
primeira versão tentar usar o driver do H2.)

### Erros de validação
| Opção | O que oferece | O que custa |
|---|---|---|
| Deixar o padrão do Spring | Nenhum código | Resposta `400` verbosa, difícil de ler no cliente |
| Tratar em cada controller | Controle total | Repetição; mistura HTTP com tratamento de erro |
| **Um `@RestControllerAdvice` global** | Um único lugar para todos os erros | Uma classe a mais |

### CORS global
O navegador bloqueia chamadas de uma página (`http://localhost:4200`) para outra origem (`:8080`), a
menos que a API autorize. Configurei isso uma vez, para todas as rotas, em vez de anotar cada controller.

## ADR — Postgres em produção e H2 local

**Contexto.** O H2 em memória é ótimo para estudar, mas perde tudo ao reiniciar. Os dois usuários
precisam que os gastos fiquem guardados.

**Decisão.** Produção usa PostgreSQL; desenvolvimento continua com H2 em memória, selecionados por
perfil Spring.

**Consequências.**
- Bom: dados duráveis em produção; desenvolvimento rápido.
- Ruim: dois bancos diferentes. Uma consulta que funciona no H2 pode se comportar diferente no Postgres, e
  o schema em produção é mantido por `ddl-auto=update`, sem histórico de migrações.
- Reverter custa: trocar o banco exige migrar os dados e ajustar a configuração de datasource.
  O [step-04](step-04-render-neon-deploy-migration.md) escolhe onde o Postgres roda.

## Como o código funciona

### `GlobalExceptionHandler`
```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public Map<String, Object> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return Map.of("status", 422, "errors", errors);
    }
}
```
- `@RestControllerAdvice` faz a classe interceptar exceções lançadas por **qualquer** controller e
  devolver o retorno como JSON.
- `MethodArgumentNotValidException` é lançada quando `@Valid` no controller rejeita o corpo da requisição.
- O handler percorre os erros de campo e monta `{"status":422,"errors":{"description":"..."}}`.
- `422 Unprocessable Entity` significa "o JSON é válido, mas os dados não fazem sentido", mais preciso que `400`.

### `WebConfig`
```java
registry.addMapping("/**")
        .allowedOrigins("http://localhost:4200", "<origem do frontend em produção>")
        .allowedMethods("GET", "POST")
        .allowedHeaders("*");
```
`addMapping("/**")` vale para todas as rotas. A lista de origens e métodos é uma **lista de permissões**:
o que não está nela é bloqueado pelo navegador. Os métodos começaram em `GET` e `POST` e crescem
conforme os endpoints ([step-05](step-05-delete-expense-endpoint.md) e [step-08](step-08-put-expense-editing.md)).
O CORS é aplicado pelo **navegador**: `curl` e apps nativos não são afetados.

### `application-prod.properties`
Monta a URL JDBC com as variáveis `PG*`, define o driver, usa `ddl-auto=update`, desliga o console do H2 e o log de SQL.

## Como verificar
1. **Caminho feliz.** Suba a API (`./mvnw spring-boot:run` ou Run no IntelliJ) e crie um gasto válido
   (ver [step-01](step-01-spring-boot-initial-setup.md)). Esperado: `201`.
2. **Persistência.** Em produção (perfil `prod`), os dados sobrevivem a um reinício; localmente, não.
3. **Estados limite.** Envie um corpo com vários campos inválidos de uma vez:
   ```bash
   curl -i -X POST http://localhost:8080/expenses \
     -H "Content-Type: application/json" \
     -d '{"description":"","amount":-5,"date":"2026-10-05","category":"FIXED"}'
   ```
   Esperado: `422` e `errors` listando `description` e `amount`.
4. **Caminho de erro / CORS.** Simule o navegador enviando o cabeçalho `Origin`:
   ```bash
   curl -i http://localhost:8080/expenses?month=2026-10 -H "Origin: http://localhost:4200"
   curl -i http://localhost:8080/expenses?month=2026-10 -H "Origin: http://site-nao-autorizado.com"
   ```
   O primeiro deve trazer `Access-Control-Allow-Origin: http://localhost:4200`; o segundo, `403 Invalid CORS request`.
5. **Regressão.** `GET /expenses` continua `200`.

## Conexão com o próximo passo
Com o perfil de produção definido, o [step-04](step-04-render-neon-deploy-migration.md) escolhe onde API e
banco rodam e cuida do SSL do Neon.
