# 0006 — Mover o Postgres para um servidor doméstico via Tailscale

- **Status:** Proposto
- **Relacionado:** [0001](0001-hospedagem-no-render-e-neon.md), [0003](0003-remover-keep-warm-apos-incidente-de-cota.md)

## Contexto

Depois do incidente de cota do Neon
([0003](0003-remover-keep-warm-apos-incidente-de-cota.md)), a vontade é reduzir
a dependência de planos gratuitos de serviços online e ter mais autonomia sobre
o banco.

Foi levantado um menu de alternativas; a escolhida foi a "Opção 3": mover
**apenas o Postgres** para um servidor doméstico (Raspberry Pi ou mini PC)
rodando em Docker, **mantendo a API no Render**.

O desafio técnico: o container do Render não tem acesso a TUN/`CAP_NET_ADMIN`,
então o Tailscale precisa rodar em modo *userspace-networking* + SOCKS5, e o
driver JDBC precisa discar através desse proxy.

## Decisão

Ainda **não decidido em definitivo** — proposta registrada para retomar numa
sessão futura.

Plano detalhado (arquitetura validada contra o código-fonte do pgjdbc, passos
de migração de dados, backup e verificação) salvo em
`~/.claude/plans/eu-gostei-da-solu-o-magical-sunrise.md`.

Arquivos que seriam criados/alterados:

- `Dockerfile`, `docker/entrypoint.sh` (novo)
- `src/main/java/com/homebudget/config/TailscaleSocksSocketFactory.java` (novo)
- `src/main/resources/application-prod.properties`
- `infra/home-postgres/docker-compose.yml` (novo, Postgres + backup)

Evolução futura esboçada ("Opção 1"): depois de estável, self-hostar também a
API no mesmo Pi e aposentar o Render.

## Consequências

- **Bom:** sem cota de compute; controle total sobre o banco e os backups.
- **Ruim:** disponibilidade passa a depender da casa (energia, internet, o
  próprio Pi); complexidade nova (túnel Tailscale, socket factory custom).
- **Pré-requisito:** reler o plano salvo antes de retomar — pode estar
  desatualizado se algo mudar no Neon/Render nesse meio-tempo.
