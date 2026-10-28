# Step 08 — Editar um gasto: `PUT /expenses/{id}`

## Objetivo
Permitir corrigir um gasto já registrado (descrição, valor, data ou categoria) sem apagá-lo e recriá-lo.
No mesmo período o frontend ganhou o modo de edição; essa parte vive no repositório do frontend e aqui
só importa o que ela exige da API.

## Decisões

### `PUT` ou `PATCH`
| Opção | O que oferece | O que custa |
|---|---|---|
| `PATCH` (atualização parcial) | Cliente manda só o que mudou | Precisa distinguir "campo ausente" de "campo nulo"; mais código |
| **`PUT` (substituição completa)** | Mesmo corpo do `POST`; validação reaproveitada | O cliente envia todos os campos mesmo que só um mude |

Como o formulário do frontend já tem todos os campos, `PUT` é mais simples e reaproveita
`ExpenseRequest` e suas validações. O custo é pequeno: são quatro campos.

### O que não pode mudar
`expenseId` vem da URL e `createdAt` é marcado `updatable = false` ([step-01](step-01-spring-boot-initial-setup.md)),
então o cliente não consegue alterá-los.

### Id inexistente
Reaproveita o padrão do [step-05](step-05-delete-expense-endpoint.md): `ExpenseNotFoundException` → `404`.

## Como o código funciona

### `ExpenseController.update`
```java
@PutMapping("/{id}")
public ExpenseResponse update(@PathVariable UUID id, @RequestBody @Valid ExpenseRequest request) {
    return service.update(id, request);
}
```
Combina o que já existia: o `id` da URL (`@PathVariable`) e o corpo validado (`@RequestBody @Valid`).
Sem `@ResponseStatus`, o status é `200`, e o corpo devolve o gasto atualizado.

### `ExpenseService.update`
```java
public ExpenseResponse update(UUID id, ExpenseRequest request) {
    Expense expense = repository.findById(id)
            .orElseThrow(() -> new ExpenseNotFoundException(id));
    expense.setDescription(request.description());
    expense.setAmount(request.amount());
    expense.setDate(request.date());
    expense.setCategory(request.category());
    return ExpenseResponse.from(repository.save(expense));
}
```
- `findById` devolve um `Optional`; `orElseThrow` lança a exceção de domínio se estiver vazio.
- Como a entidade já tem id, `save` faz um `UPDATE` em vez de `INSERT`.

### CORS
`"PUT"` entrou em `allowedMethods` no `WebConfig`; sem isso o navegador bloquearia a edição.

## Como verificar
Suba a API (`./mvnw spring-boot:run` ou Run no IntelliJ).
1. **Caminho feliz.** Crie um gasto, copie o `expenseId` e altere:
   ```bash
   curl -i -X PUT http://localhost:8080/expenses/<expenseId> -H "Content-Type: application/json" \
     -d '{"description":"Supermercado (corrigido)","amount":18000,"date":"2026-10-06","category":"FIXED"}'
   ```
   Esperado: `200` com os campos novos, o mesmo `expenseId` e o `createdAt` original.
2. **Persistência.** `GET /expenses?month=2026-10` mostra o gasto com os valores novos e sem duplicata.
3. **Estados limite.** Mudar a `date` para outro mês faz o gasto sair da listagem do mês anterior e entrar na do novo.
4. **Caminho de erro.** UUID inexistente → `404`; `amount` negativo ou `description` vazia → `422` com `errors`:
   ```bash
   curl -i -X PUT http://localhost:8080/expenses/00000000-0000-0000-0000-000000000000 \
     -H "Content-Type: application/json" \
     -d '{"description":"x","amount":100,"date":"2026-10-06","category":"FIXED"}'
   ```
5. **Regressão.** `POST`, `GET` e `DELETE` continuam como antes.

## Conexão com o próximo passo
Com criar, listar, editar e apagar prontos, o [step-09](step-09-import-fixed-category-filter.md) acrescenta uma
operação de negócio: importar os gastos fixos do mês anterior.
