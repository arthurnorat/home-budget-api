# Step 10 — Entrada mensal: `GET` e `PUT /income`

> **Persistência substituída no [step-14](step-14-firestore-persistence.md).** A entidade JPA e o `JpaRepository`
> mostrados aqui deram lugar a um documento do Firestore cujo id é o mês. As decisões sobre o contrato (o mês como
> identidade, `PUT` que cria ou atualiza, `amount` 0 para mês sem registro) continuam valendo.

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

## Conexão com o próximo passo
O backend cobre o MVP e mais: gastos, importação de fixos e entrada mensal. O [step-11](step-11-readme-update.md)
atualiza a documentação pública do projeto.
