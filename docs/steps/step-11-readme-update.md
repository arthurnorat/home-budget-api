# Step 11 — README atualizado

## Objetivo
Fazer o `README.md` descrever o projeto como ele realmente está: stack, funcionalidades, arquitetura e
endpoints. O README original era um resumo de poucas linhas escrito antes do código existir, ainda
mencionando React, React Native e Railway ([step-02](step-02-stack-update-angular-ios.md) e
[step-04](step-04-render-neon-deploy-migration.md) mudaram isso). A reescrita aconteceu em duas fases: primeiro uma versão
longa em português com todo o contrato da API; depois uma versão bilíngue (inglês e português), mais enxuta.

## Decisões

### Onde mora o contrato da API
| Opção | O que oferece | O que custa |
|---|---|---|
| Contrato completo no README | Quem chega no GitHub vê tudo | Duplica o `CLAUDE.md` e as duas cópias divergem |
| **Contrato só no `CLAUDE.md`; README lista os endpoints e aponta para ele** | Uma única fonte da verdade; os outros repositórios importam o `CLAUDE.md` por `@import` | Quem lê só o README precisa abrir mais um arquivo |

A versão longa do README repetia o contrato inteiro. A versão atual lista os endpoints e remete ao `CLAUDE.md`.

### Idioma
README bilíngue, com um seletor de idioma no topo e as duas seções com o mesmo conteúdo. O histórico não
registra o motivo da escolha; ela segue a regra de que o repositório é em inglês, mas o leitor principal
(eu) estuda em português.

### O arquivo para outras ferramentas de IA
Neste período existiu um `AGENTS.md`, criado para o Codex, que repetia o contexto do projeto e as regras
de trabalho. Foi removido ao reorganizar `docs/sessions` em `docs/steps`. O histórico não registra o motivo
da remoção; hoje só o `CLAUDE.md` existe, como fonte de contexto.

## Como o código funciona
Este step não altera código, só `README.md`. A estrutura atual:
1. Seletor de idioma (`🇺🇸 English · 🇧🇷 Português`) e âncoras `<a name="english">` / `<a name="português">`.
2. Em cada língua: descrição, demo em produção, funcionalidades, stack, arquitetura, modelo de dados, lista
   de endpoints, convenções.
3. Regra do projeto: qualquer alteração em uma língua é espelhada na outra.

## Como verificar
1. **Caminho feliz.** Abra o `README.md` no GitHub ou no preview do IntelliJ e clique nos dois links do seletor
   de idioma: cada um leva à sua seção.
2. **Persistência.** Confira que o arquivo está no repositório na `main`.
3. **Estados limite.** As duas línguas têm as mesmas seções e os mesmos endpoints, na mesma ordem.
4. **Caminho de erro.** Os links de demonstração abrem (frontend e API):
   ```bash
   curl -s -o /dev/null -w "%{http_code}\n" "https://home-budget-api-wml2.onrender.com/expenses?month=2026-10"
   ```
   Esperado: `200`.
5. **Regressão.** A lista de endpoints bate com o contrato do `CLAUDE.md`.

## Conexão com o próximo passo
Com a documentação em dia, o [step-12](step-12-keep-warm-workflow.md) trata da hibernação dos planos gratuitos.
