# Bolão Copa do Mundo 2026

Web app para gerenciar o bolão da Copa do Mundo 2026, substituindo a planilha Excel usada em edições anteriores.

## Funcionalidades

- Apostas de placar por jogo com prazo automático de encerramento
- Apostas especiais: classificação por grupo, semifinalistas, artilheiro
- Pontuação configurável pelo administrador
- Ranking em tempo real
- Painel admin para lançar resultados e gerenciar usuários

## Stack

- **Backend:** Spring Boot 3.3.x + Java 21
- **Frontend:** Thymeleaf + HTMX + Bootstrap 5
- **Banco:** PostgreSQL 16
- **Migrações:** Flyway
- **Auth:** Spring Security 6 (sessão server-side)
- **Build:** Maven

## Rodando localmente

### Pré-requisitos

- Java 21
- PostgreSQL 16 rodando em `localhost:5432`

### Configuração

Crie o banco e o usuário no PostgreSQL:

```sql
CREATE USER bolao WITH PASSWORD 'bolao';
CREATE DATABASE bolao OWNER bolao;
```

### Iniciando

```bash
./deploy.sh dev
```

O app sobe em http://localhost:8080.

**Usuário admin padrão:** `admin` / `admin123`

### Outros comandos

```bash
./deploy.sh build    # gera o JAR
./deploy.sh start    # sobe tudo com Docker Compose
./deploy.sh stop     # para os containers
./deploy.sh logs     # acompanha os logs
./deploy.sh db       # sobe apenas o PostgreSQL via Docker
./deploy.sh clean    # remove containers, volumes e JAR
```

## Deploy com Docker

```bash
cp .env.example .env   # configure as variáveis
./deploy.sh start
```

O `docker-compose.yml` sobe o app e o PostgreSQL juntos. O app ficará disponível na porta definida em `APP_PORT` (padrão: 8080).

## Variáveis de ambiente

| Variável | Padrão | Descrição |
|---|---|---|
| `BOLAO_SUBTITLE` | `Câmara dos Lordes` | Subtítulo exibido na tela de login |
| `BOLAO_PAYMENT_AMOUNT` | `R$ 150` | Texto do valor de entrada (exibição) |
| `BOLAO_PAYMENT_KEY` | — | Chave PIX / dados de pagamento (use `\|` para múltiplas linhas) |
| `BOLAO_CONTACT_URL` | — | URL do botão de contato (ex: WhatsApp) |
| `BOLAO_CONTACT_LABEL` | — | Texto do botão de contato |
| `BOLAO_PAYMENT_AMOUNT_VALUE` | `150` | Valor numérico por participante (para cálculo de premiação) |
| `BOLAO_CURRENCY_SYMBOL` | `R$` | Símbolo da moeda (ex: `R$`, `$`) |
| `BOLAO_HOSTING_COST` | `50` | Custo de hospedagem na moeda local, descontado do prêmio |

## Deploy em produção

### Infraestrutura

- **Servidor:** AWS Lightsail — 1 VM, plano $12/mês (2 GB RAM), região us-east-1
- **DNS:** DuckDNS (dynamic DNS gratuito)
- **SSL:** Let's Encrypt via certbot

### Duas instâncias na mesma VM

| | Bolão dos Lordes (BRL) | Bolão Mapocho (CLP) |
|---|---|---|
| Domínio | `bolaolords.duckdns.org` | `bolaomapocho.duckdns.org` |
| Diretório | `/opt/bolao` | `/opt/bolao2` |
| Porta host | 8080 (interna via nginx) | 8081 |
| `BOLAO_PAYMENT_AMOUNT_VALUE` | `150` | `20000` |
| `BOLAO_CURRENCY_SYMBOL` | `R$` | `$` |
| `BOLAO_HOSTING_COST` | `50` | `10000` |

O nginx (container Docker em `/opt/bolao/`) age como proxy reverso para os dois domínios.

### Deploy automático (GitHub Actions)

A cada push em `main`:
1. Build do JAR (Maven + Java 21)
2. Build e push da imagem → `ghcr.io/roamorim/bolao-wc-2026:latest`
3. SSH na VM → `cd /opt/bolao && docker compose pull && docker compose up -d`
4. SSH na VM → `cd /opt/bolao2 && docker compose pull && docker compose up -d`

Secrets necessários no repositório: `LIGHTSAIL_HOST`, `LIGHTSAIL_SSH_KEY`.

### Atualizando o docker-compose.yml no servidor

> **Atenção:** o GitHub Actions atualiza apenas a **imagem Docker** — o `docker-compose.yml` em cada diretório do servidor é uma cópia manual e precisa ser atualizada manualmente quando houver mudanças (ex: novas variáveis de ambiente).

```bash
# no servidor, via SSH
sudo nano /opt/bolao/docker-compose.yml    # instância BRL
sudo nano /opt/bolao2/docker-compose.yml   # instância CLP
sudo docker compose up -d
```

O `docker-compose.yml` do servidor deve usar a imagem do registry (não `build: .`):

```yaml
app:
  image: ghcr.io/roamorim/bolao-wc-2026:latest
```

## Testes

```bash
./mvnw test
```
