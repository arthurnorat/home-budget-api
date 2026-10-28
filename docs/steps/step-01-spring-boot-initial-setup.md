# Step 01 — Projeto Spring Boot e primeiros endpoints de gastos

## Objetivo
Criar o esqueleto do backend: um projeto Spring Boot com Maven que registra gastos
(`POST /expenses`) e lista os gastos de um mês (`GET /expenses`), guardando-os num banco H2 em
memória durante o desenvolvimento. Este step define a forma como todo o resto do projeto é
organizado: camadas, DTOs e o modelo de dados.

## Decisões

### Camadas e organização por feature
| Opção | O que oferece | O que custa |
|---|---|---|
| Pacotes por camada (`controller/`, `service/`, `repository/`) | Fácil de achar "todos os controllers" | Uma feature nova espalha arquivos em vários pacotes |
| **Pacotes por feature** (`expense/` com tudo dentro) | Tudo de uma feature junto; fácil de remover ou isolar | Código compartilhado precisa de um pacote próprio |

Escolhi por feature (`com.orcamento.expense`). O projeto nasceu com o pacote `com.orcamento`; o
[step-07](step-07-project-rename.md) o renomeou.

### Entidade separada dos DTOs
A entidade `Expense` representa a linha da tabela; `ExpenseRequest` e `ExpenseResponse` representam o
JSON de entrada e saída. Se a API devolvesse a entidade direto, qualquer mudança no banco mudaria o
contrato público, e campos internos vazariam. O custo é escrever um método de conversão.

### `Integer` em centavos e `LocalDate`
Valores monetários em `double` acumulam erro de arredondamento (0,1 + 0,2 ≠ 0,3). Guardar inteiros em
centavos evita isso. Datas usam `LocalDate` (sem fuso horário), pois um gasto é "do dia", não de um instante.

### Banco local
O H2 em memória sobe junto com a aplicação e dispensa instalar Postgres. Em contrapartida, os dados
somem a cada reinício. Para estudo isso é uma vantagem: todo teste manual começa limpo.

## ADR — Valores em centavos e ausência de autenticação

**Contexto.** O app é de uso privado por duas pessoas conhecidas, com finalidade de aprendizado.
Dinheiro exige exatidão, e implementar login agora atrasaria o que realmente se quer estudar.

**Decisão.**
1. Todo valor monetário é um inteiro em **centavos** (R$ 150,00 = `15000`), no banco, na API e nos clientes.
2. **Não há autenticação** no MVP: qualquer um com a URL acessa e os dados são compartilhados sem
   identificação de autoria.

**Consequências.**
- Bom: sem erros de ponto flutuante; API simples; nenhum código de login para manter.
- Ruim: todo cliente precisa converter centavos para reais ao exibir; qualquer pessoa que descubra a URL
  lê e altera os dados.
- Reverter custa: trocar o formato do dinheiro exigiria migrar a coluna e mudar todos os clientes;
  adicionar autenticação mexe em todos os controllers e em todos os clientes, e exigiria decidir a quem
  pertence cada gasto (hoje não existe essa informação).

## Como o código funciona

### `Expense` (entidade)
```java
@Entity
@Table(name = "expenses")
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "expense_id")
    private UUID expenseId;
    ...
    @Enumerated(EnumType.STRING)
    private Category category;
    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
```
- `@Entity` marca a classe como mapeada para uma tabela; `@Table` dá o nome.
- `@Id` + `@GeneratedValue(strategy = UUID)`: o próprio Hibernate gera o identificador.
- `@Enumerated(EnumType.STRING)` grava `"FIXED"` em vez do número 0; assim, reordenar o enum não
  corrompe os dados.
- `updatable = false` impede que um `UPDATE` altere `created_at`.

### `ExpenseRequest` e `ExpenseResponse` (DTOs)
Ambos são `record`s: classes imutáveis, sem setters, com construtor e acessores gerados.
`ExpenseRequest` carrega as regras de validação (`@NotBlank`, `@NotNull`, `@Positive`); o controller as
ativa com `@Valid`. `ExpenseResponse.from(expense)` converte a entidade em resposta.

### `ExpenseRepository`
```java
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    List<Expense> findByDateBetweenOrderByDateDesc(LocalDate start, LocalDate end);
}
```
O Spring Data JPA implementa a interface sozinho. O **nome do método** é lido como uma consulta:
`findBy` + `Date` + `Between` + `OrderByDateDesc` vira
`WHERE date BETWEEN ? AND ? ORDER BY date DESC`.

### `ExpenseService` e `ExpenseController`
O controller só lida com HTTP (`@PostMapping`, `@GetMapping`, `@ResponseStatus(CREATED)`); o service
guarda a lógica. `listByMonth(YearMonth)` calcula o primeiro e o último dia do mês
(`month.atDay(1)` e `month.atEndOfMonth()`) e chama o repository. Sem o parâmetro `month`, o
controller usa o mês atual. As dependências chegam por **injeção pelo construtor**: o Spring cria
o `ExpenseRepository` e o entrega ao `ExpenseService`.

### Configuração
`application.properties` define o H2 em memória com `ddl-auto=create-drop` (cria as tabelas ao subir e as
apaga ao parar), mostra o SQL no console e habilita o console do H2. `application-prod.properties`
nasceu preparado para Postgres; o [step-03](step-03-postgres-cors-exception-handling.md) o ajusta.

## Como verificar
Suba a API: no IntelliJ, execute `HomeBudgetApplication`; na CLI:
```bash
./mvnw spring-boot:run
```
1. **Caminho feliz.** Crie e liste um gasto.
   ```bash
   curl -i -X POST http://localhost:8080/expenses \
     -H "Content-Type: application/json" \
     -d '{"description":"Supermercado","amount":15000,"date":"2026-10-05","category":"VARIABLE"}'
   curl -i "http://localhost:8080/expenses?month=2026-10"
   ```
   Esperado: `201` com `expenseId` gerado e `createdAt` preenchido; depois `200` com o gasto no array.
2. **Persistência.** Abra `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:home-budget-api`, usuário
   `sa`, senha vazia) e rode `SELECT * FROM expenses`. A linha existe com `category = 'VARIABLE'`.
   Reiniciar a aplicação apaga tudo, o que é esperado no H2 em memória.
3. **Estados limite.** `GET /expenses?month=2026-11` devolve `200` e `[]`. Um gasto em `2026-10-31` aparece
   em outubro e não em novembro (limite do mês).
4. **Caminho de erro.** Envie `amount` negativo ou `description` vazia: a resposta deve ser rejeitada.
   (O formato da rejeição é tratado no [step-03](step-03-postgres-cors-exception-handling.md); aqui
   ainda pode aparecer como erro `400` padrão do Spring.)
5. **Regressão.** Não há funcionalidade anterior.

## Conexão com o próximo passo
Com a base pronta, o [step-02](step-02-stack-update-angular-ios.md) fecha a decisão sobre os clientes
(Angular e Swift) e o [step-03](step-03-postgres-cors-exception-handling.md) prepara o banco de produção,
o CORS e o tratamento de erros.
