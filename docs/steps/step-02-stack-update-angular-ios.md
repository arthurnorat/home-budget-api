# Step 02 — Definição da stack dos clientes: Angular e Swift

## Objetivo
Reduzir o escopo do projeto antes de ele crescer. O plano inicial previa React na web, React Native
para um app mobile único e apps nativos em Swift e Kotlin. Este step troca isso por **um cliente web
(Angular) e um app nativo (Swift, para iPhone)**. Não há mudança de código: é uma decisão registrada no
`CLAUDE.md`, que serve de contexto para todo o trabalho seguinte.

## Decisões

### Cliente web
| Opção | O que oferece | O que custa |
|---|---|---|
| React (plano inicial) | Ecossistema enorme, curva suave | Mais uma biblioteca para decidir tudo em volta (rotas, estado, formulários) |
| **Angular** | Framework completo e opinativo, com TypeScript, injeção de dependência e formulários embutidos | Curva de aprendizado maior no início |

Angular combina melhor com o que já estou estudando no backend: a injeção de dependência e a separação
em camadas lembram o Spring.

### Clientes mobile
| Opção | O que oferece | O que custa |
|---|---|---|
| React Native (app único para iOS e Android) | Um código para duas plataformas | Mais uma linguagem e um ecossistema fora do foco do estudo |
| Swift e Kotlin (dois apps nativos) | Melhor experiência em cada plataforma | Dobra o trabalho |
| **Só Swift, app nativo para iPhone** | Foco na minha especialização (Apple) | Quem usa Android fica só com o cliente web |

Venceu Swift porque o projeto existe para aprendizado e Swift/iOS é a minha stack. Quem usa Android
acessa o cliente web.

## ADR — Clientes: Angular na web e Swift no iPhone

**Contexto.** Há dois usuários, um no iPhone e outro no Android, e um backend que será consumido por mais
de um cliente. A prioridade é aprender, não cobrir todas as plataformas.

**Decisão.** A ordem de construção passa a ser três etapas: backend (Spring Boot), cliente web em Angular
hospedado no Vercel e app nativo em Swift. React, React Native e Kotlin saem do plano.

**Consequências.**
- Bom: escopo menor e coerente com o que estou estudando; o contrato da API (JSON sobre HTTP) serve a
  qualquer cliente.
- Ruim: nenhum app nativo para Android.
- Reverter custa pouco para o backend (a API não depende do cliente); custa o tempo de construir o novo
  cliente. O que mais pesa no futuro é o CORS ([step-03](step-03-postgres-cors-exception-handling.md) e
  [step-06](step-06-cors-vercel-production.md)), que precisa listar as origens de cada cliente web.

## Como o código funciona
Nenhum arquivo de código muda. O que muda é o `CLAUDE.md`: o objetivo passa a falar em "app nativo
Swift" e as seções de stack e de ordem de construção refletem as três etapas acima.

## Como verificar
Como não há código, a verificação é de coerência:
1. **Caminho feliz.** Abra o `CLAUDE.md` do projeto e confirme que a stack cita Angular e Swift e não cita
   React, React Native nem Kotlin.
2. **Persistência.** A decisão está registrada no repositório, não só na conversa.
3. **Estados limite, erro e regressão.** Não se aplicam: a API continua igual. Rode a API ([step-01](step-01-spring-boot-initial-setup.md))
   se quiser ter certeza de que nada quebrou.

## Conexão com o próximo passo
Sabendo que o cliente principal será uma página Angular rodando em outro endereço (`localhost:4200`), o
[step-03](step-03-postgres-cors-exception-handling.md) libera o CORS para ele e prepara o banco de produção.
