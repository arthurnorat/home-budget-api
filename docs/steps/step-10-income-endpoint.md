# Step 10 — Entrada mensal: `GET` e `PUT /income`

## Objetivo
Registrar quanto dinheiro entra em cada mês, para que os clientes possam comparar entrada e gastos.
São dois endpoints: `GET /income?month=yyyy-MM` consulta o valor do mês e `PUT /income?month=yyyy-MM`
salva ou atualiza. É a segunda feature do backend (depois de `expense`) e ganha seu próprio pacote, `income`, seguindo a
organização por feature do [step-01](step-01-spring-boot-initial-setup.md).

## Decisões

### Como identificar a entrada de um mês
| Opção | O que oferece | O que custa |
|---|---|---|
| Id UUID gerado + coluna `month` | Padrão igual ao de `Expense` | Precisa garantir unicidade do mês e fazer uma consulta extra |
| **O próprio mês (`yyyy-MM`) como chave primária** | Um mês só pode ter um valor, sem regra extra; busca direta por id | A chave tem significado de negócio e é uma `String` |

Há exatamente uma entrada por mês, então o mês **é** a identidade. A chave é `String` (e não `YearMonth`)
porque o JPA não mapeia `YearMonth` de forma nativa; o service converte com `month.toString()`.

### `PUT` cria ou atualiza
`PUT` é idempotente: enviar o mesmo valor duas vezes dá o mesmo resultado. O service busca o mês; se não
existir, cria. O cliente não precisa saber se o registro já existia.

### `GET` de mês sem registro
Em vez de `404`, devolve `{"month":"2026-06","amount":0}`. Mês sem entrada cadastrada não é um erro para a
tela; ela precisa de um número para exibir.

### Validação
`@NotNull @PositiveOrZero`: zero é permitido (mês sem entrada), negativo não.

## Como o código funciona

### `MonthlyIncome` (entidade)
```java
@Entity
@Table(name = "monthly_income")
public class MonthlyIncome {
    @Id
    private String month; // format yyyy-MM

    @Column(nullable = false)
    private Integer amount; // in cents
    ...
}
```
Sem `@GeneratedValue`: quem define o id é a aplicação. Valores em centavos, como em todo o projeto
([step-01](step-01-spring-boot-initial-setup.md)).

### `MonthlyIncomeRepository`
```java
public interface MonthlyIncomeRepository extends JpaRepository<MonthlyIncome, String> {}
```
Vazia: `findById`, `save` e o resto já vêm do `JpaRepository`, com `String` como tipo do id.

### `MonthlyIncomeService`
```java
public IncomeResponse get(YearMonth month) {
    return repository.findById(month.toString())
            .map(IncomeResponse::from)
            .orElse(new IncomeResponse(month.toString(), 0));
}

public IncomeResponse save(YearMonth month, int amount) {
    MonthlyIncome income = repository.findById(month.toString())
            .orElse(new MonthlyIncome(month.toString(), 0));
    income.setAmount(amount);
    return IncomeResponse.from(repository.save(income));
}
```
`Optional.map` converte o valor se existir; `orElse` define o padrão. No `save`, o mesmo objeto serve para
os dois casos: encontrado (vira `UPDATE`) ou novo (vira `INSERT`).

### `MonthlyIncomeController`
O parâmetro `month` é opcional nos dois métodos e cai para `YearMonth.now()`. `IncomeRequest` e
`IncomeResponse` são `record`s, separados da entidade como em `expense`.

## Como verificar
Suba a API; o H2 começa vazio.
1. **Caminho feliz.**
   ```bash
   curl -i -X PUT "http://localhost:8080/income?month=2026-10" -H "Content-Type: application/json" -d '{"amount":500000}'
   curl -i "http://localhost:8080/income?month=2026-10"
   ```
   Esperado: `200` com `{"month":"2026-10","amount":500000}` nas duas.
2. **Persistência.** `SELECT * FROM monthly_income` no console do H2 (`http://localhost:8080/h2-console`) mostra uma linha.
   Um segundo `PUT` com outro valor **altera** a linha, não cria outra.
3. **Estados limite.** `GET /income?month=2026-11` (nunca salvo) devolve `200` com `amount` `0`.
   `PUT` com `{"amount":0}` é aceito.
4. **Caminho de erro.**
   ```bash
   curl -i -X PUT "http://localhost:8080/income?month=2026-10" -H "Content-Type: application/json" -d '{"amount":-1}'
   curl -i "http://localhost:8080/income?month=outubro"
   ```
   Esperado: `422` com `errors` no primeiro e `400` no segundo.
5. **Regressão.** Os endpoints de `/expenses` continuam funcionando.

## Conexão com o próximo passo
O backend cobre o MVP e mais: gastos, importação de fixos e entrada mensal. O [step-11](step-11-readme-update.md)
atualiza a documentação pública do projeto.
