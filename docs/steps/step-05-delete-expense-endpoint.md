# Step 05 — Remover um gasto: `DELETE /expenses/{id}`

## Objetivo
Permitir apagar um gasto pelo seu id. O step também introduz a primeira **exceção de domínio**
(`ExpenseNotFoundException`), para que apagar um id que não existe responda `404` e não `500`.

## Decisões

### Como reagir a um id inexistente
| Opção | O que oferece | O que custa |
|---|---|---|
| Chamar `deleteById` direto | Uma linha | Em Spring Data JPA 3.x apagar um id inexistente é silencioso: o cliente recebe sucesso sem nada ter sido apagado |
| Devolver `404` em silêncio dentro do controller | Resposta correta | Lógica de negócio no controller |
| **Verificar no service e lançar uma exceção de domínio, mapeada num handler global** | `404` coerente em todas as rotas; controller continua fino | Uma classe de exceção e um handler a mais |

Escolhi a terceira: o service decide que "não existe" é um caso de erro (`existsById`), e o
`GlobalExceptionHandler` do [step-03](step-03-postgres-cors-exception-handling.md) traduz isso em HTTP.

### Código de status
`204 No Content` para sucesso: a operação funcionou e não há nada a devolver. `200` com corpo vazio
seria ambíguo.

## Como o código funciona

### `ExpenseNotFoundException`
```java
public class ExpenseNotFoundException extends RuntimeException {
    public ExpenseNotFoundException(UUID id) {
        super("Expense not found: " + id);
    }
}
```
Estende `RuntimeException` (exceção não verificada): quem chama não é obrigado a declará-la nem tratá-la,
e ela sobe até o handler global. A mensagem carrega o id, útil para depurar.

### `ExpenseService.delete`
```java
public void delete(UUID id) {
    if (!repository.existsById(id)) {
        throw new ExpenseNotFoundException(id);
    }
    repository.deleteById(id);
}
```

### `GlobalExceptionHandler`
```java
@ExceptionHandler(ExpenseNotFoundException.class)
@ResponseStatus(HttpStatus.NOT_FOUND)
public Map<String, Object> handleNotFound(ExpenseNotFoundException ex) {
    return Map.of("status", 404, "error", ex.getMessage());
}
```

### `ExpenseController.delete` e CORS
`@DeleteMapping("/{id}")` com `@PathVariable UUID id` extrai o id da URL; `@ResponseStatus(NO_CONTENT)`
define o `204`. No `WebConfig`, `"DELETE"` entrou na lista de métodos permitidos; sem isso o navegador
bloquearia a chamada do frontend ([step-03](step-03-postgres-cors-exception-handling.md)).

## Conexão com o próximo passo
O frontend já pode apagar gastos, mas está num domínio novo; o [step-06](step-06-cors-vercel-production.md)
libera a origem de produção do Vercel. O mesmo padrão de `404` será reaproveitado pelo `PUT` do
[step-08](step-08-put-expense-editing.md).
