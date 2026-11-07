#!/usr/bin/env bash
#
# Levanta PostgreSQL para desarrollo local.
#
# - Si hay Docker disponible, usa docker compose (postgres:16).
# - Si no hay Docker, descarga PostgreSQL 17 en formato portable dentro de
#   .local/ y lo ejecuta como el usuario actual, sin necesitar sudo.
#
# Uso:
#   ./scripts/local-postgres.sh start
#   ./scripts/local-postgres.sh stop
#   ./scripts/local-postgres.sh status
#
# Variables opcionales: PGPORT, POSTGRES_DB, POSTGRES_USER (mismos nombres que
# usa la aplicación y docker-compose.yml).
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOCAL_DIR="$ROOT_DIR/.local"
CACHE_DIR="$LOCAL_DIR/cache"
PG_DIR="$LOCAL_DIR/postgres"
DATA_DIR="$PG_DIR/data"
LOG_FILE="$PG_DIR/postgres.log"

PG_VERSION="17.5.0"
PG_PORT="${PGPORT:-5432}"
PG_USER="${POSTGRES_USER:-tp1_user}"
PG_DB="${POSTGRES_DB:-tp1_favoritos}"

ACTION="${1:-start}"

if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
  case "$ACTION" in
    start)
      (cd "$ROOT_DIR" && docker compose up -d)
      ;;
    stop)
      (cd "$ROOT_DIR" && docker compose stop)
      ;;
    status)
      (cd "$ROOT_DIR" && docker compose ps)
      ;;
    *)
      echo "Uso: $0 {start|stop|status}" >&2
      exit 1
      ;;
  esac
  exit 0
fi

case "$ACTION" in
  stop)
    if [ -x "$PG_DIR/bin/pg_ctl" ] && "$PG_DIR/bin/pg_ctl" -D "$DATA_DIR" status >/dev/null 2>&1; then
      "$PG_DIR/bin/pg_ctl" -D "$DATA_DIR" -m fast -w stop
      echo "PostgreSQL detenido."
    else
      echo "PostgreSQL local no está corriendo."
    fi
    exit 0
    ;;
  status)
    if [ -x "$PG_DIR/bin/pg_ctl" ] && "$PG_DIR/bin/pg_ctl" -D "$DATA_DIR" status >/dev/null 2>&1; then
      echo "PostgreSQL local corriendo en localhost:$PG_PORT (base $PG_DB, usuario $PG_USER)."
    else
      echo "PostgreSQL local detenido."
    fi
    exit 0
    ;;
  start)
    ;;
  *)
    echo "Uso: $0 {start|stop|status}" >&2
    exit 1
    ;;
esac

# 1. Descargar y extraer los binarios portables si todavía no están.
if [ ! -x "$PG_DIR/bin/postgres" ]; then
  mkdir -p "$CACHE_DIR" "$PG_DIR"
  JAR="$CACHE_DIR/embedded-postgres-binaries-linux-amd64-$PG_VERSION.jar"
  if [ ! -f "$JAR" ]; then
    echo "Descargando PostgreSQL $PG_VERSION portable..."
    curl -fL --retry 3 --progress-bar \
      -o "$JAR" \
      "https://repo.maven.apache.org/maven2/io/zonky/test/postgres/embedded-postgres-binaries-linux-amd64/$PG_VERSION/embedded-postgres-binaries-linux-amd64-$PG_VERSION.jar"
  fi

  TMP_DIR="$(mktemp -d)"
  trap 'rm -rf "$TMP_DIR"' EXIT
  (cd "$TMP_DIR" && unzip -q "$JAR")
  TXZ="$(find "$TMP_DIR" -maxdepth 1 -name '*.txz' | head -n 1)"
  if [ -z "$TXZ" ]; then
    echo "No se encontró el archivo .txz dentro del binario portable." >&2
    exit 1
  fi
  tar -xJf "$TXZ" -C "$PG_DIR"
  rm -rf "$TMP_DIR"
  trap - EXIT
fi

# 2. Inicializar el cluster la primera vez.
if [ ! -f "$DATA_DIR/PG_VERSION" ]; then
  mkdir -p "$DATA_DIR"
  "$PG_DIR/bin/initdb" -D "$DATA_DIR" -U "$PG_USER" --auth=trust --encoding=UTF8 --locale=C >/dev/null
  echo "Cluster de PostgreSQL inicializado para el usuario $PG_USER."
fi

# 3. Crear la base si no existe. Los binarios portables de zonky no incluyen
#    psql/createdb, así que se usa el backend en modo single-user (con el
#    servidor detenido).
database_exists() {
  printf "SELECT datname FROM pg_database WHERE datname = '%s';\n" "$PG_DB" \
    | "$PG_DIR/bin/postgres" --single -D "$DATA_DIR" postgres 2>/dev/null \
    | grep -q "datname = \"$PG_DB\""
}

if "$PG_DIR/bin/pg_ctl" -D "$DATA_DIR" status >/dev/null 2>&1; then
  SERVER_RUNNING="yes"
else
  SERVER_RUNNING="no"
fi

if [ "$SERVER_RUNNING" = "no" ] && ! database_exists; then
  echo "Creando base de datos '$PG_DB'..."
  printf 'CREATE DATABASE "%s";\n' "$PG_DB" \
    | "$PG_DIR/bin/postgres" --single -D "$DATA_DIR" postgres >/dev/null
fi

# 4. Arrancar el servidor.
if [ "$SERVER_RUNNING" = "yes" ]; then
  echo "PostgreSQL ya está corriendo en localhost:$PG_PORT."
else
  "$PG_DIR/bin/pg_ctl" -D "$DATA_DIR" -l "$LOG_FILE" -o "-p $PG_PORT" -w start >/dev/null
  echo "PostgreSQL iniciado en localhost:$PG_PORT (datos en $DATA_DIR)."
fi

echo "Listo. La aplicación puede conectarse con:"
echo "  DB_HOST=localhost DB_PORT=$PG_PORT POSTGRES_DB=$PG_DB POSTGRES_USER=$PG_USER"
