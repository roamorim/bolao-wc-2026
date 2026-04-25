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

## Testes

```bash
./mvnw test
```
