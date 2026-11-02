#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"

# Spring Boot no lee archivos .env: cargamos gradox2/.env (si existe) para que
# las credenciales locales de SMTP y el resto de la configuración se apliquen.
# Se parsea KEY=VALUE en vez de hacer `source`, porque los valores pueden tener
# espacios (p. ej. la app password de Gmail tal como la muestra Google).
load_env_file() {
  local file="$1" line key value
  [[ -f "$file" ]] || return 0
  while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line%$'\r'}"
    [[ "$line" =~ ^[[:space:]]*(#|$) ]] && continue
    [[ "$line" == export\ * ]] && line="${line#export }"
    [[ "$line" != *=* ]] && continue
    key="${line%%=*}"
    value="${line#*=}"
    key="${key//[[:space:]]/}"
    [[ "$key" =~ ^[A-Za-z_][A-Za-z0-9_]*$ ]] || continue
    if [[ "$value" == \"*\" || "$value" == \'*\' ]]; then
      value="${value:1:${#value}-2}"
    fi
    export "$key=$value"
  done < "$file"
}

load_env_file "$PROJECT_ROOT/.env"

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"

case "${1:-dev}" in
  run)
    if ! (echo > /dev/tcp/127.0.0.1/5432) >/dev/null 2>&1; then
      echo "PostgreSQL no está disponible en localhost:5432."
      echo "Usa './run.sh dev' para levantar app + base de datos automáticamente."
      exit 1
    fi
    exec "$PROJECT_ROOT/mvnw" spring-boot:run -Dspring-boot.run.profiles="$SPRING_PROFILES_ACTIVE"
    ;;
  test)
    exec "$PROJECT_ROOT/mvnw" test
    ;;
  dev)
    export APP_UID="$(id -u)" APP_GID="$(id -g)"
    cd "$PROJECT_ROOT/../Docker"
    exec docker compose -f docker-compose.dev.yml up --build
    ;;
  docker-up)
    cd "$PROJECT_ROOT/../Docker"
    exec docker compose --env-file .env --profile full up --build
    ;;
  docker-down)
    cd "$PROJECT_ROOT/../Docker"
    exec docker compose --env-file .env --profile full down
    ;;
  dev-down)
    cd "$PROJECT_ROOT/../Docker"
    exec docker compose -f docker-compose.dev.yml down
    ;;
  *)
    echo "Usage: $0 [dev|dev-down|run|test|docker-up|docker-down]"
    exit 1
    ;;
esac
