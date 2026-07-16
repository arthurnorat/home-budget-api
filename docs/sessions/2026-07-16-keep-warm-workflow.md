# Sessão 2026-07-16 — Workflow para evitar cold start no Render e no Neon

## O que foi feito
- Diagnosticado o motivo do delay ao acessar a API após período de inatividade: dois "sonos" independentes e sobrepostos — o Render (free tier) hiberna o serviço web após ~15 min sem tráfego, e o Neon (free tier) suspende o compute do banco após ~5 min sem query
- Avaliadas as opções disponíveis: upgrade pago no Render, upgrade pago no Neon, e ping externo periódico — optado pelo ping periódico via GitHub Actions por ser gratuito e o repositório ser público
- Criado `.github/workflows/keep-warm.yml`: workflow agendado (`cron`) que chama `GET /expenses?month=<mês atual>` a cada 5 minutos, mantendo tanto a API (Render) quanto o banco (Neon) ativos, já que o endpoint executa uma query real
- Definido o intervalo de 5 minutos como o mais adequado: é o menor intervalo suportado pelo agendador do GitHub Actions, mantém a API sempre abaixo do limite de 15 min do Render, e minimiza a janela em que o Neon pode suspender (limite de ~5 min)
- Testada a execução manual (`workflow_dispatch`) e confirmado o disparo automático agendado na aba Actions do GitHub
- Identificada e explicada a causa de uma dúvida sobre o app: a lista de gastos não atualiza sozinha entre dispositivos porque a API é REST tradicional (sem push) — o Angular só busca os dados uma vez ao carregar a página. Ficou como próximo passo (a ser feito no repositório do frontend `home-budget-web`): combinar polling leve com atualização ao voltar o foco da aba

## Commits
- `d4fc2e6` — chore: add scheduled workflow to keep API and database warm

## Observação
O ajuste do polling/refresh automático no frontend Angular ficou pendente para uma sessão futura, no repositório `home-budget-web` (fora do escopo deste repositório de backend).
