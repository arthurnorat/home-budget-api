# Orçamento — Contexto do Projeto

## Objetivo
Aplicativo de controle de gastos domésticos compartilhado entre dois usuários (um no iPhone, outro no Android).

## Perfil do Desenvolvedor
- Estudante em transição de desenvolvimento mobile (Swift/iOS) para backend
- Este projeto tem finalidade de aprendizado
- Quando solicitado, explicações devem ser didáticas, detalhadas e com exemplos práticos
- Conhecimentos prévios: Swift, Objective-C, iOS, Git, noções de Java

## Stack

### Backend
- **Java + Spring Boot** — API REST
- **PostgreSQL** — banco de dados relacional
- **Railway** — hospedagem do backend e do banco

### Frontend Web
- **React** — interface web para uso e testes
- **Vercel** — hospedagem do frontend

### Frontend Mobile (futuro)
- **React Native** — app único para iOS e Android

### Apps Nativos (futuro)
- **Swift** — app nativo para iOS
- **Kotlin** — app nativo para Android

## Ordem de Construção
1. Backend (Spring Boot + PostgreSQL)
2. Frontend web (React) — para testar e usar o backend
3. Frontend mobile (React Native)
4. Apps nativos (Swift e Kotlin)

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

## Status Atual
Projeto em fase inicial. Backend sendo construído primeiro.
