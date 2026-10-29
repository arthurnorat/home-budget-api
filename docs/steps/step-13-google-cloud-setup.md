# Step 13 — Infraestrutura no Google Cloud: Firestore e Cloud Run

## Objetivo
Preparar a base em nuvem da API: um projeto no Google Cloud com faturamento e alerta de orçamento, um banco
**Firestore** em São Paulo, uma **conta de serviço** para a API rodar no **Cloud Run** e o **emulador do
Firestore** para desenvolver localmente. Este step não altera o código da API; o código vem no
[step-14](step-14-firestore-persistence.md) e a publicação no step-15.

## Decisões

### Banco de dados
| Opção | Vantagens | Desvantagens |
|---|---|---|
| **Firestore** | Não suspende por inatividade; a cota gratuita é medida em operações e armazenamento, não em horas de compute; usa a mesma conta e as mesmas permissões do restante do Google Cloud | Banco de documentos: sem JOIN e com consultas limitadas ao que os índices permitem; prende o projeto ao Google |
| Neon (PostgreSQL) | SQL completo, JOINs e transações; modelo relacional que o projeto já usava | O plano gratuito suspende o compute após cerca de 5 minutos sem consultas e limita as horas mensais de compute; quando a cota acaba, o banco é pausado |

Escolhi o Firestore porque o volume de dados é pequeno (duas pessoas, poucos documentos por mês) e as consultas
são simples: por mês e por categoria. Os recursos do SQL que o Neon oferece não seriam usados, e as limitações do
plano gratuito dele são justamente as que mais atrapalham num app de uso esporádico.

### Hospedagem da API
| Opção | Vantagens | Desvantagens |
|---|---|---|
| **Cloud Run** | Roda a mesma imagem Docker; escala a zero e só cobra durante as requisições; integra-se ao Firestore por identidade de serviço, sem chaves | Cold start da JVM na primeira requisição após inatividade (alguns segundos); exige conta de faturamento ativa |
| Cloud Run com `min-instances=1` | Sem cold start | Instância sempre ligada: tem custo mesmo sem tráfego |
| Render | Deploy automático a cada push; plano gratuito simples | O serviço gratuito hiberna após cerca de 15 minutos sem tráfego; fica fora do restante da infraestrutura Google |

Escolhi o Cloud Run com `min-instances=0`: o cold start é aceitável para o uso do app, e continua sem custo
quando ninguém está usando.

### Região
`southamerica-east1` (São Paulo): menor latência para quem usa o app. A região do Firestore **não pode ser
alterada depois de criada**.

### Cota gratuita e preços
- **Firestore:** a [página de preços do Firebase](https://firebase.google.com/docs/firestore/pricing) lista 1 GiB
  armazenado, 50 mil leituras, 20 mil gravações e 20 mil exclusões por dia, e 10 GiB de saída de rede por mês,
  para um banco por projeto. Os preços variam por localização.
- **Cloud Run:** a cota e o nível de preço de cada região estão na
  [página de preços do Cloud Run](https://cloud.google.com/run/pricing). Para o uso de duas pessoas, o consumo
  esperado é pequeno, mas a confirmação dos números atuais deve ser feita direto nessa página.
- Sobre `min-instances` e cobrança de instância ociosa, veja a
  [documentação de instâncias mínimas](https://docs.cloud.google.com/run/docs/configuring/min-instances).

## ADR — Firestore e Cloud Run no Google Cloud

**Contexto.** O app é de uso privado por duas pessoas, sem orçamento para infraestrutura, e serve também para
eu aprender Google Cloud. O uso é esporádico: ficar horas sem nenhuma requisição é normal.

**Decisão.** Firestore (edição Standard, modo nativo, banco `(default)`, `southamerica-east1`, regras de
segurança restritivas) como banco e Cloud Run (`min-instances=0`) como hospedagem, com a imagem Docker do
projeto. Os endpoints da API não mudam para os clientes. As regras restritivas valem para acesso direto de
navegador ou celular; a API acessa pelo servidor, com a identidade da conta de serviço, e não é afetada por elas.

**Consequências.**
- Bom: nada é suspenso por inatividade; não há cota de horas de compute; escala a zero.
- Ruim: o modelo passa a ser de documentos, sem JOIN e sem as transações SQL que a API tinha; a camada de
  persistência precisa ser escrita para o Firestore ([step-14](step-14-firestore-persistence.md)); cold start da
  JVM; dependência do Google.
- Um orçamento do Google **só avisa**, não bloqueia gastos. O limite real vem de `--max-instances` no
  Cloud Run (step-15) e de manter `min-instances=0`.
- Reverter custa: trocar de banco exige reescrever os repositories e mover os dados; trocar de hospedagem exige só
  reconfigurar a publicação, porque a imagem Docker é portátil.

## O que foi construído

| Recurso | Para que serve |
|---|---|
| Projeto no Google Cloud | Agrupa tudo; é a unidade de faturamento e de permissões |
| Vínculo com a conta de faturamento | O Cloud Run exige faturamento ativo, mesmo dentro da cota gratuita |
| Orçamento com alertas (50% e 100%) | Avisa por e-mail se o gasto passar do valor definido |
| APIs ativadas | Firestore, Cloud Run, Cloud Build e Artifact Registry |
| Banco Firestore `(default)`, edição Standard | Guarda as coleções `expenses` e `monthly_income` (step-14); o primeiro banco do projeto tem a cota gratuita |
| Conta de serviço `home-budget-api-run` | Identidade com a qual a API roda no Cloud Run |
| Papel `roles/datastore.user` | Permite à conta de serviço ler e gravar no Firestore, e nada mais |
| Emulador do Firestore | Firestore local, para desenvolver sem tocar no banco real |

A API não usa chaves JSON. No Cloud Run ela recebe as credenciais da conta de serviço automaticamente
(Application Default Credentials), e nenhum segredo vai para o repositório.

## Conexão com o próximo passo
Com o Firestore e o emulador prontos, o [step-14](step-14-firestore-persistence.md) escreve a camada de
persistência da API para o Firestore, mantendo os endpoints idênticos.
