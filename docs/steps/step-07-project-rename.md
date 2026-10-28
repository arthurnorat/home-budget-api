# Step 07 — Renomear o projeto: de `orcamento` para `home-budget-api`

## Objetivo
Trocar o nome do projeto em todos os lugares onde ele aparece, para o código, o repositório e as
ferramentas falarem a mesma língua: pacote Java `com.orcamento` → `com.homebudget`, diretório
`orcamento` → `home-budget-api` e repositório do GitHub `orcamento-domestico` → `home-budget-api`.

## Decisões

### Renomear agora ou nunca
Um nome em português misturado com código em inglês (convenção do projeto) ficaria mais caro de corrigir a
cada endpoint novo. O projeto ainda era pequeno (poucas classes), então foi o momento barato.

### Como renomear o pacote
| Opção | O que oferece | O que custa |
|---|---|---|
| Editar `package` e imports à mão | Nenhuma ferramenta | Fácil esquecer um arquivo; só falha ao compilar |
| **Refatoração do IDE (Rename/Move package)** | Atualiza declaração, imports e referências juntos | Depende do IDE e deve ser revisada |

### Ordem
Primeiro o código (pacote), depois o diretório local, depois o repositório remoto e, por fim, o `remote`
do Git local, para nunca ficar com o ambiente apontando para um nome que já não existe.

## ADR — Pacote Java `com.homebudget`

**Contexto.** O nome do pacote aparece no `package` de cada classe, nos imports, no `groupId` do `pom.xml`
e na estrutura de pastas de `src/main/java`.

**Decisão.** Adotar `com.homebudget` como raiz do código e `home-budget-api` como nome do projeto.
O `pom.xml` também teve o `groupId` e o nome ajustados.

**Consequências.**
- Bom: nome consistente em toda a cadeia (código, repositório, URLs, documentação).
- Ruim: o histórico do Git mistura o caminho antigo e o novo; ferramentas que guardaram o nome antigo
  (como o cache do IDE) precisam ser corrigidas à mão.
- Reverter custa: refazer a mesma refatoração em todos os arquivos; por isso vale decidir o nome cedo.

## Como o código funciona
A mudança de código é mecânica: cada arquivo troca a declaração do pacote e os imports.
```java
// antes
package com.orcamento.expense;
// depois
package com.homebudget.expense;
```
Arquivos afetados: `HomeBudgetApplication` (antes `OrcamentoApplication`), `WebConfig`, o pacote
`exception`, o pacote `expense` com seus DTOs, e o teste gerado pelo Initializr. O comportamento da API não muda.

Fora do código:
- **Diretório local.** Renomeado de `~/Java_Projects/orcamento` para `~/Java_Projects/home-budget-api`.
- **GitHub.** O repositório foi renomeado; o `remote` local passou a apontar para o novo endereço.
- **IntelliJ.** O IDE continuou mostrando o nome de módulo antigo (`[orcamento]`) mesmo depois de limpar os
  arquivos `.idea/`. A origem era o cache do sistema de build externo do IntelliJ, em
  `~/Library/Caches/JetBrains/<versão>/projects/…/external_build_system/`, e foi corrigida lá. Referências
  antigas foram removidas de `.idea/` e um arquivo `home-budget-api.iml` foi criado na raiz.
- **Resíduo.** O commit do pacote também registrou por engano uma cópia do projeto sob
  `.claude/worktrees/`, que foi removida depois. O `.gitignore` agora ignora essa pasta.

## Como verificar
1. **Caminho feliz.** Compile e suba:
   ```bash
   ./mvnw clean package -DskipTests
   ./mvnw spring-boot:run
   ```
   Esperado: `BUILD SUCCESS` e a aplicação sobe na porta 8080. No IntelliJ: Run em `HomeBudgetApplication`.
2. **Persistência.** Crie e liste um gasto (ver [step-01](step-01-spring-boot-initial-setup.md)).
3. **Estados limite.** `grep -r "orcamento" src pom.xml` não deve achar nada além de histórico em comentários
   (se achar, é resíduo a corrigir).
4. **Caminho de erro.** Um import esquecido apareceria como erro de compilação (`package com.orcamento… does not exist`).
5. **Regressão.** `DELETE` com UUID inexistente ainda responde `404` ([step-05](step-05-delete-expense-endpoint.md)).

## Conexão com o próximo passo
Com o nome estável, o [step-08](step-08-put-expense-editing.md) adiciona a edição de gastos.
