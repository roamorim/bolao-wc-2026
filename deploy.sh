#!/usr/bin/env bash
# deploy.sh — Script de deploy do Bolão Copa 2026
# Uso: ./deploy.sh <comando>
#
# Comandos:
#   dev        Inicia o app em modo desenvolvimento (Maven + Postgres local/docker)
#   build      Compila e gera o JAR
#   start      Sobe tudo com Docker Compose (produção)
#   stop       Para os containers Docker Compose
#   restart    Para e reinicia os containers
#   logs       Acompanha os logs do app em produção
#   db         Sobe apenas o PostgreSQL (útil para dev)
#   status     Mostra status dos containers
#   clean      Remove containers, volumes e o JAR gerado
#   help       Exibe esta ajuda

set -euo pipefail

# ─── Configuração ───────────────────────────────────────────────────────────

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$PROJECT_DIR/.env"

# Detecta Java 21 (tenta JAVA_HOME definido, depois locais comuns)
find_java21() {
  local candidates=(
    "${JAVA_HOME:-}"
    "/home/linuxbrew/.linuxbrew/opt/openjdk@21/libexec"
    "/usr/lib/jvm/java-21-openjdk-amd64"
    "/usr/lib/jvm/temurin-21"
    "/opt/homebrew/opt/openjdk@21/libexec"
    "/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"
  )
  for candidate in "${candidates[@]}"; do
    if [[ -n "$candidate" && -x "$candidate/bin/java" ]]; then
      local version
      version=$("$candidate/bin/java" -version 2>&1 | head -1)
      if echo "$version" | grep -qE '"21\.'; then
        echo "$candidate"
        return 0
      fi
    fi
  done
  return 1
}

setup_java() {
  if java_home=$(find_java21); then
    export JAVA_HOME="$java_home"
    export PATH="$JAVA_HOME/bin:$PATH"
    echo "  Java 21: $JAVA_HOME"
  else
    echo "⚠  Java 21 não encontrado. Tentando com o Java disponível..."
    if ! java -version 2>&1 | grep -qE '"(21|2[2-9]|[3-9][0-9])\.'; then
      echo "✗ Java 21+ é necessário. Instale com: brew install openjdk@21"
      exit 1
    fi
  fi
}

load_env() {
  if [[ -f "$ENV_FILE" ]]; then
    # shellcheck disable=SC2046
    export $(grep -v '^#' "$ENV_FILE" | grep -v '^$' | xargs)
    echo "  .env carregado"
  else
    echo "  Usando variáveis padrão (sem .env)"
  fi
}

check_docker() {
  if ! command -v docker &>/dev/null; then
    echo "✗ Docker não encontrado. Instale o Docker: https://docs.docker.com/get-docker/"
    exit 1
  fi
  if ! docker info &>/dev/null 2>&1; then
    echo "✗ Docker está instalado mas não está rodando."
    exit 1
  fi
}

# ─── Comandos ───────────────────────────────────────────────────────────────

cmd_dev() {
  echo "▶  Modo desenvolvimento"
  setup_java
  load_env

  # Sobe só o postgres se o docker estiver disponível
  if command -v docker &>/dev/null && docker info &>/dev/null 2>&1; then
    echo "  Subindo PostgreSQL..."
    docker compose -f "$PROJECT_DIR/docker-compose.yml" up -d postgres
    echo "  Aguardando PostgreSQL ficar pronto..."
    sleep 3
  else
    echo "  Docker não disponível — certifique-se de que o PostgreSQL está rodando em localhost:5432"
  fi

  echo "  Iniciando Spring Boot (porta ${PORT:-8080})..."
  cd "$PROJECT_DIR"
  ./mvnw spring-boot:run \
    -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=default" \
    -Dspring-boot.run.arguments="--spring.datasource.url=${DB_URL:-jdbc:postgresql://localhost:5432/${DB_NAME:-bolao}}"
}

cmd_build() {
  echo "▶  Build"
  setup_java
  cd "$PROJECT_DIR"
  echo "  Compilando e gerando JAR..."
  ./mvnw clean package -DskipTests -q
  jar_file=$(ls target/*.jar 2>/dev/null | head -1)
  echo "✓  JAR gerado: $jar_file"
}

cmd_start() {
  echo "▶  Start (Docker Compose)"
  check_docker
  load_env

  # Garante que o JAR existe
  if ! ls "$PROJECT_DIR"/target/*.jar &>/dev/null; then
    echo "  JAR não encontrado — executando build..."
    cmd_build
  fi

  cd "$PROJECT_DIR"
  docker compose up -d --build
  echo "✓  App rodando em http://localhost:${APP_PORT:-8080}"
  echo "   Logs: ./deploy.sh logs"
}

cmd_stop() {
  echo "▶  Stop"
  check_docker
  cd "$PROJECT_DIR"
  docker compose down
  echo "✓  Containers parados"
}

cmd_restart() {
  echo "▶  Restart"
  check_docker
  load_env
  cd "$PROJECT_DIR"
  docker compose down
  docker compose up -d --build
  echo "✓  App reiniciado em http://localhost:${APP_PORT:-8080}"
}

cmd_logs() {
  check_docker
  cd "$PROJECT_DIR"
  docker compose logs -f app
}

cmd_db() {
  echo "▶  Subindo apenas o PostgreSQL"
  check_docker
  load_env
  cd "$PROJECT_DIR"
  docker compose up -d postgres
  echo "✓  PostgreSQL disponível em localhost:5432"
  echo "   Banco: ${DB_NAME:-bolao} | Usuário: ${DB_USERNAME:-bolao}"
}

cmd_status() {
  check_docker
  cd "$PROJECT_DIR"
  docker compose ps
}

cmd_clean() {
  echo "▶  Limpando..."
  cd "$PROJECT_DIR"
  if command -v docker &>/dev/null && docker info &>/dev/null 2>&1; then
    docker compose down -v --remove-orphans 2>/dev/null || true
    echo "  Containers e volumes removidos"
  fi
  setup_java
  ./mvnw clean -q
  echo "✓  Pronto"
}

cmd_help() {
  echo "Uso: ./deploy.sh <comando>"
  echo ""
  echo "Comandos disponíveis:"
  echo "  dev        Inicia em modo dev (Maven + Postgres via Docker)"
  echo "  build      Compila e gera o JAR"
  echo "  start      Sobe tudo com Docker Compose (produção)"
  echo "  stop       Para os containers"
  echo "  restart    Para e reinicia os containers"
  echo "  logs       Acompanha os logs do app"
  echo "  db         Sobe apenas o PostgreSQL"
  echo "  status     Mostra status dos containers"
  echo "  clean      Remove containers, volumes e o JAR"
  echo "  help       Exibe esta ajuda"
  echo ""
  echo "Primeiro uso:"
  echo "  cp .env.example .env   # configure as variáveis de ambiente"
  echo "  ./deploy.sh dev        # desenvolvimento local"
  echo "  ./deploy.sh start      # produção via Docker"
}

# ─── Entrypoint ─────────────────────────────────────────────────────────────

echo ""
echo "╔══════════════════════════════╗"
echo "║   Bolão Copa 2026            ║"
echo "╚══════════════════════════════╝"
echo ""

COMMAND="${1:-help}"

case "$COMMAND" in
  dev)     cmd_dev ;;
  build)   cmd_build ;;
  start)   cmd_start ;;
  stop)    cmd_stop ;;
  restart) cmd_restart ;;
  logs)    cmd_logs ;;
  db)      cmd_db ;;
  status)  cmd_status ;;
  clean)   cmd_clean ;;
  help|--help|-h) cmd_help ;;
  *)
    echo "Comando desconhecido: '$COMMAND'"
    echo "Use: ./deploy.sh help"
    exit 1
    ;;
esac
