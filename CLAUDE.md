# Orçamento — Contexto do Projeto

## Objetivo
Aplicativo de controle de gastos domésticos compartilhado entre dois usuários (um no iPhone, outro no Android — futuramente via app nativo Swift).

## Perfil do Desenvolvedor
- Estudante em transição de desenvolvimento mobile (Swift/iOS) para backend
- Este projeto tem finalidade de aprendizado
- Quando solicitado, explicações devem ser didáticas, detalhadas e com exemplos práticos
- Conhecimentos prévios: Swift, Objective-C, iOS, Git, noções de Java

## Stack

### Backend
- **Java + Spring Boot** — API REST
- **PostgreSQL** — banco de dados relacional
- **Render** — hospedagem do backend e do banco

### Frontend Web
- **Angular** — interface web para uso e testes
- **Vercel** — hospedagem do frontend

### App iOS (futuro)
- **Swift** — app nativo para iPhone

## Ordem de Construção
1. Backend (Spring Boot + PostgreSQL)
2. Frontend web (Angular) — para testar e usar o backend
3. App iOS nativo (Swift)

## MVP — Funcionalidades
- Registrar gasto: descrição, valor, data e categoria
- Categorias: Fixo ou Variável (novas categorias serão adicionadas no futuro)
- Listar gastos do mês atual
- Gastos compartilhados entre os dois usuários sem identificação de autoria

## Autenticação
Não há autenticação no MVP. O app é de uso privado entre dois usuários conhecidos.
Identificação por usuário poderá ser implementada no futuro.

## Modelo de Dados

### Tabela: expenses
- `expense_id` (PK) — UUID gerado automaticamente
- `description` — descrição do gasto
- `amount` — valor em centavos (integer), exibido como R$ 0,00
- `date` — data no formato ISO 8601 (YYYY-MM-DD), exibida como DD/MM/YYYY
- `category` — "fixed" ou "variable"
- `created_at` — timestamp de criação

## Convenções
- Valores monetários armazenados em centavos (integer)
- Datas armazenadas em ISO 8601, exibidas em DD/MM/YYYY
- Código e variáveis em inglês
- Commits em inglês
- Comentários no código em português são permitidos

## Contrato da API

Este arquivo é a fonte de verdade da API. O frontend e os apps mobile importam este CLAUDE.md via `@import` para ter acesso a este contrato sem duplicação.

### URLs
- **Desenvolvimento:** `http://localhost:8080`
- **Produção:** `https://home-budget-api-wml2.onrender.com`

### Endpoints

**POST /expenses** — Criar gasto (201)
```json
// Request
{ "description": "Supermercado", "amount": 15000, "date": "2026-04-25", "category": "VARIABLE" }

// Response
{ "expenseId": "uuid", "description": "Supermercado", "amount": 15000, "date": "2026-04-25", "category": "VARIABLE", "createdAt": "2026-04-25T10:30:00Z" }
```

**GET /expenses?month=yyyy-MM** — Listar gastos do mês (200)
```json
// Response — array de gastos no mesmo formato acima
```

**PUT /expenses/{id}** — Atualizar gasto (200)
```json
// Request — mesmo formato do POST
// Response — gasto atualizado no mesmo formato acima
```

**DELETE /expenses/{id}** — Remover gasto (204, sem body)

**POST /expenses/import-fixed?month=yyyy-MM** — Importar fixos do mês anterior (201)
```json
// Response — array de gastos criados (cópias dos FIXED do mês anterior, com date = dia 1 do mês solicitado)
```

### Campos e tipos
| Campo | Tipo | Observação |
|-------|------|------------|
| `expenseId` | UUID (string) | Gerado automaticamente |
| `description` | string | Obrigatório, mínimo 1 caractere |
| `amount` | integer | Em **centavos** (R$ 1,00 = 100) |
| `date` | string ISO 8601 | Formato `YYYY-MM-DD` |
| `category` | string enum | `"FIXED"` ou `"VARIABLE"` |
| `createdAt` | string ISO 8601 | Timestamp UTC, gerado automaticamente |

### CORS
Origens permitidas atualmente: `http://localhost:4200`, `https://home-budget-web-ten.vercel.app`

Para adicionar um app mobile: inserir a origem (ou scheme nativo, ex: `capacitor://localhost`) em `WebConfig.java`.

### Autenticação
Nenhuma no MVP. O app é de uso privado.

## Status Atual
Backend MVP completo e em produção no Render.
URL de produção: `https://home-budget-api-wml2.onrender.com`

### Sessão 2026-04-25
**O que foi feito:**
- Implementado endpoint `DELETE /expenses/{id}` (204 No Content)
- Criada `ExpenseNotFoundException` com handler 404 no `GlobalExceptionHandler`
- Adicionado método `delete(UUID)` no `ExpenseService`
- Adicionado `DELETE` nos métodos CORS permitidos no `WebConfig`
- Deploy realizado no Render (auto-deploy via push no GitHub)

### Sessão 2026-04-26
**O que foi feito:**
- CORS atualizado no `WebConfig.java` para permitir a URL do Vercel: `https://home-budget-web-ten.vercel.app`
- Redeploy no Render (auto-deploy via push no GitHub)

### Sessão 2026-04-27
**O que foi feito:**
- Diretório renomeado: `~/Java_Projects/orcamento` → `~/Java_Projects/home-budget-api`
- Pacote Java migrado de `com.orcamento` para `com.homebudget` (commit `9b5ed47`)
- Repositório GitHub renomeado de `orcamento-domestico` para `home-budget-api`
- Remote git local atualizado para `https://github.com/arthurnorat/home-budget-api.git`
- Referências antigas ao nome `orcamento` removidas dos arquivos `.idea/` do IntelliJ
- Corrigido nome do módulo IntelliJ (`[orcamento]` → `home-budget-api`) editando o cache externo em `~/Library/Caches/JetBrains/IntelliJIdea2026.1/projects/home-budget-api.d764aca5/external_build_system/`
- Criado `home-budget-api.iml` na raiz do projeto

### Sessão 2026-04-28
**O que foi feito:**
- Backend: implementado endpoint `PUT /expenses/{id}` (200 OK com `ExpenseResponse`)
- Backend: adicionado método `update(UUID, ExpenseRequest)` no `ExpenseService`
- Backend: adicionado `PUT` nos métodos CORS permitidos no `WebConfig`
- Frontend: adicionado `updateExpense()` no `ExpenseService` Angular
- Frontend: `ExpenseForm` agora suporta modo de edição (input `editingExpense`, outputs `expenseUpdate` e `cancelEdit`)
- Frontend: `ExpenseTable` ganhou botão de editar (✎) com output `editExpense`
- Frontend: `App` orquestra o fluxo de edição com signal `editingExpense`
- Frontend (`home-budget-app`): localizado em `~/Web_Development_Projects/home-budget-app`

**Próxima sessão:**
- Deploy do frontend no Vercel (push para o repositório do frontend)
- Testes manuais em produção

### Sessão 2026-05-02
**O que foi feito:**
- Frontend: adicionado filtro de categoria na `ExpenseTable` (Variável / Fixo / Todos) com padrão "Variável"
- Frontend: coluna de categoria removida da tabela (redundante com o filtro)
- Frontend: ajustes de responsividade mobile (padding, font-size, text-overflow)
- Backend: implementado endpoint `POST /expenses/import-fixed?month=yyyy-MM`
- Backend: adicionado `findByCategoryAndDateBetweenOrderByDateDesc` no `ExpenseRepository`
- Backend: adicionado método `importFixed(YearMonth)` no `ExpenseService`
- Frontend: adicionado botão "Importar" na filter bar que copia fixos do mês anterior
- CLAUDE.md do backend expandido com contrato completo da API (para uso pelos projetos mobile via `@import`)
