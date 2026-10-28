# Step 09 — Importar gastos fixos do mês anterior: `POST /expenses/import-fixed`

## Objetivo
Gastos fixos (aluguel, internet, mensalidades) se repetem todo mês. Em vez de recadastrá-los, este
endpoint copia os gastos `FIXED` do mês anterior para o mês pedido, com data no dia 1. No frontend, no
mesmo período, entraram o botão "Importar" e um filtro de categoria (Variável, Fixo, Todos); o filtro é
só visual e não existe na API.

## Decisões

### Quem filtra por categoria
| Opção | O que oferece | O que custa |
|---|---|---|
| Parâmetro `category` no `GET /expenses` | Menos dados trafegando | Mais lógica na API; cada filtro novo vira código no backend |
| **Filtrar no frontend** | A API continua simples; troca de filtro é instantânea | Baixa mais dados que o necessário (irrelevante nesta escala) |

### Quem faz a cópia
Fazer o frontend ler os fixos e chamar `POST` um por um seria lento e deixaria o estado pela metade se uma
chamada falhasse. Um **endpoint de negócio** no backend faz tudo de uma vez.

### Data dos itens copiados
Todos vão para o **dia 1** do mês pedido. Os gastos fixos não têm um dia confiável a preservar, e o dia 1
deixa o resultado previsível.

### Risco aceito: duplicatas
Chamar o endpoint duas vezes copia duas vezes. A API não verifica se o mês já foi importado. O frontend é quem
oferece o botão, e para dois usuários isso foi considerado aceitável; fica registrado como limitação.

## Como o código funciona

### `ExpenseRepository`
```java
List<Expense> findByCategoryAndDateBetweenOrderByDateDesc(Category category, LocalDate start, LocalDate end);
```
Mesma ideia do [step-01](step-01-spring-boot-initial-setup.md): o nome do método vira
`WHERE category = ? AND date BETWEEN ? AND ? ORDER BY date DESC`.

### `ExpenseService.importFixed`
```java
public List<ExpenseResponse> importFixed(YearMonth month) {
    YearMonth previousMonth = month.minusMonths(1);
    List<Expense> fixed = repository.findByCategoryAndDateBetweenOrderByDateDesc(
            Category.FIXED, previousMonth.atDay(1), previousMonth.atEndOfMonth());

    LocalDate importDate = month.atDay(1);
    List<Expense> copies = fixed.stream().map(source -> {
        Expense copy = new Expense();
        copy.setDescription(source.getDescription());
        copy.setAmount(source.getAmount());
        copy.setDate(importDate);
        copy.setCategory(Category.FIXED);
        return copy;
    }).toList();

    return repository.saveAll(copies).stream().map(ExpenseResponse::from).toList();
}
```
- `YearMonth.minusMonths(1)` lida sozinho com a virada de ano (`2027-01` → `2026-12`).
- Cada cópia é uma entidade **nova** (sem id), então vira `INSERT` com novo `expenseId` e novo `createdAt`.
- `saveAll` salva a lista numa só operação.

### `ExpenseController.importFixed`
`@PostMapping("/import-fixed")` com `month` **obrigatório** (`@RequestParam`, sem `required = false`) e
`@ResponseStatus(CREATED)`: `201`, porque a chamada cria recursos novos.

## Como verificar
Suba a API; o H2 começa vazio.
1. **Caminho feliz.** Crie dois fixos e um variável em outubro, depois importe para novembro:
   ```bash
   curl -s -X POST http://localhost:8080/expenses -H "Content-Type: application/json" \
     -d '{"description":"Aluguel","amount":150000,"date":"2026-10-05","category":"FIXED"}'
   curl -s -X POST http://localhost:8080/expenses -H "Content-Type: application/json" \
     -d '{"description":"Internet","amount":10000,"date":"2026-10-10","category":"FIXED"}'
   curl -s -X POST http://localhost:8080/expenses -H "Content-Type: application/json" \
     -d '{"description":"Mercado","amount":30000,"date":"2026-10-12","category":"VARIABLE"}'
   curl -i -X POST "http://localhost:8080/expenses/import-fixed?month=2026-11"
   ```
   Esperado: `201` com um array de **2** gastos (Aluguel e Internet), ambos `FIXED` e com `date` `2026-11-01`.
2. **Persistência.** `GET /expenses?month=2026-11` lista os dois; `GET ...?month=2026-10` continua com os três originais.
3. **Estados limite.** Importar para um mês cujo anterior não tem fixos devolve `201` e `[]`. Importar para
   `2027-01` busca em `2026-12`. Importar duas vezes duplica (limitação conhecida).
4. **Caminho de erro.** Sem `month` ou com formato inválido:
   ```bash
   curl -i -X POST "http://localhost:8080/expenses/import-fixed"
   curl -i -X POST "http://localhost:8080/expenses/import-fixed?month=novembro"
   ```
   Esperado: `400` nos dois casos.
5. **Regressão.** `POST`, `GET`, `PUT` e `DELETE` continuam como antes.

## Conexão com o próximo passo
Falta registrar quanto entra por mês para comparar com os gastos: o [step-10](step-10-income-endpoint.md)
cria o endpoint de entrada mensal.
