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

Este arquivo é a fonte de verdade da API. O frontend e os apps mobile importam este AGENTS.md via `@import` para ter acesso a este contrato sem duplicação.

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

**GET /income?month=yyyy-MM** — Buscar entrada do mês (200)
```json
// Response
{ "month": "2026-06", "amount": 500000 }
// Retorna amount=0 se nenhuma entrada foi registrada para o mês
```

**PUT /income?month=yyyy-MM** — Salvar ou atualizar entrada do mês (200)
```json
// Request
{ "amount": 500000 }
// Response
{ "month": "2026-06", "amount": 500000 }
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
Backend completo e em produção no Render. Frontend em produção no Vercel.
- Backend: `https://home-budget-api-wml2.onrender.com`
- Frontend: `https://home-budget-web-ten.vercel.app`
