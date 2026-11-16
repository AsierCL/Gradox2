#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"

# Spring Boot no lee archivos .env: cargamos gradox2/.env (si existe) para que
# las credenciales locales de SMTP y el resto de la configuración se apliquen.
# Se parsea KEY=VALUE en vez de hacer `source`, porque los valores pueden tener
# espacios (p. ej. la app password de Gmail tal como la muestra Google).
# Lo que ya llega definido en el entorno del proceso manda: el archivo solo
# rellena lo que falta, igual que la precedencia de Spring Boot (entorno > .env).
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
    # El entorno del proceso tiene prioridad: si ya viene definida, no se toca.
    [[ -v "$key" ]] && continue
    if [[ "$value" == \"*\" || "$value" == \'*\' ]]; then
      value="${value:1:${#value}-2}"
    fi
    export "$key=$value"
  done < "$file"
}

load_env_file "$PROJECT_ROOT/.env"

export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-local}"
export GRADOX_API_PORT="${GRADOX_API_PORT:-8080}"
export GRADOX_WEB_PORT="${GRADOX_WEB_PORT:-5173}"

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
    echo "Gradox dev: API en http://localhost:${GRADOX_API_PORT} · web en http://localhost:${GRADOX_WEB_PORT}"
    exec docker compose -f docker-compose.dev.yml up --build
    ;;
  web)
    cd "$PROJECT_ROOT/../frontend"
    export VITE_API_URL="${VITE_API_URL:-http://localhost:${GRADOX_API_PORT}}"
    exec npm run dev -- --host 0.0.0.0 --port "$GRADOX_WEB_PORT"
    ;;
  web-install)
    cd "$PROJECT_ROOT/../frontend"
    exec npm ci
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
    echo "Usage: $0 [dev|dev-down|run|web|web-install|test|docker-up|docker-down]"
    echo "  dev         Levanta API + PostgreSQL + MinIO + web (Docker)"
    echo "  dev-down    Detiene el entorno de desarrollo"
    echo "  run         Ejecuta solo la API contra PostgreSQL/MinIO locales"
    echo "  web         Ejecuta solo el frontend con Vite"
    echo "  web-install Instala las dependencias del frontend"
    echo "  test        Ejecuta la suite del backend"
    exit 1
    ;;
esac
