#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"

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
