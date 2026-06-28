#!/usr/bin/env bash
# Busca os confrontos REAIS de uma fase do mata-mata na football-data.org
# e gera o SQL para gravar os times certos nas partidas internas,
# contornando a lógica de sorteio do BracketAssemblyService (que não
# segue a tabela oficial de alocação dos terceiros colocados da FIFA).
#
# A numeração oficial de partidas da FIFA é sempre cronológica dentro de
# cada fase, então em vez de tentar mapear "1º grupo X / 3º grupo Y" para
# um match_number, este script ordena os jogos da API por utcDate e
# atribui sequencialmente a partir do match_number inicial informado.
#
# Uso:
#   FOOTBALL_DATA_API_KEY=xxx ./scripts/fetch-official-bracket.sh dump
#       Lista todos os jogos não-fase-de-grupos retornados pela API
#       (stage, data, times, status), para confirmar o nome exato da fase
#       antes de gerar qualquer SQL.
#
#   FOOTBALL_DATA_API_KEY=xxx ./scripts/fetch-official-bracket.sh sql <STAGE> <MATCH_NUMBER_INICIAL> <QTD_JOGOS>
#       Busca os jogos da fase <STAGE>, ordena por data/hora real e
#       imprime um UPDATE por jogo. Falha se a quantidade de jogos
#       confirmados pela API não bater com <QTD_JOGOS>.
#       Exemplo (Round of 32, partidas 73-88):
#         ./scripts/fetch-official-bracket.sh sql ROUND_OF_32 73 16 > /tmp/bracket_r32.sql
#
# Aplicar o SQL gerado:
#   docker exec -i bolao-postgres  psql -U bolao  -d bolao  < /tmp/bracket_r32.sql
#   docker exec -i bolao2-postgres psql -U bolao2 -d bolao2 < /tmp/bracket_r32.sql
#
# Requer: curl, jq, FOOTBALL_DATA_API_KEY no ambiente.

set -euo pipefail

: "${FOOTBALL_DATA_API_KEY:?defina FOOTBALL_DATA_API_KEY no ambiente}"

BASE_URL="https://api.football-data.org/v4"
COMPETITION="WC"

fetch_matches() {
  curl -sf -H "X-Auth-Token: $FOOTBALL_DATA_API_KEY" \
    "$BASE_URL/competitions/$COMPETITION/matches"
}

cmd_dump() {
  fetch_matches | jq -r '
    .matches[]
    | select(.stage != "GROUP_STAGE")
    | [.stage, .utcDate, .homeTeam.tla, .awayTeam.tla, .status] | @tsv' \
    | sort -k1,1 -k2,2
}

cmd_sql() {
  local stage="$1" start="$2" count="$3"
  local rows
  rows="$(fetch_matches | jq -r --arg stage "$stage" '
    .matches[]
    | select(.stage == $stage)
    | [.utcDate, .homeTeam.tla, .awayTeam.tla] | @tsv' | sort)"

  local n_rows=0
  if [[ -n "$rows" ]]; then
    n_rows="$(printf '%s\n' "$rows" | wc -l)"
  fi

  if [[ "$n_rows" -ne "$count" ]]; then
    echo "✗ Esperava $count jogos confirmados na fase '$stage', a API retornou $n_rows." >&2
    echo "  Confira o nome da fase e quais jogos já têm time confirmado com: $0 dump" >&2
    exit 1
  fi

  echo "BEGIN;"
  local i=0
  while IFS=$'\t' read -r date home away; do
    local match_number=$((start + i))
    printf "UPDATE matches SET home_team_id = (SELECT id FROM teams WHERE code = '%s'), away_team_id = (SELECT id FROM teams WHERE code = '%s'), match_datetime = '%s' WHERE match_number = %d; -- %s x %s\n" \
      "$home" "$away" "$date" "$match_number" "$home" "$away"
    i=$((i + 1))
  done <<< "$rows"
  echo "COMMIT;"
}

case "${1:-}" in
  dump)
    cmd_dump
    ;;
  sql)
    if [[ $# -ne 4 ]]; then
      echo "Uso: $0 sql <STAGE> <MATCH_NUMBER_INICIAL> <QTD_JOGOS>" >&2
      exit 1
    fi
    cmd_sql "$2" "$3" "$4"
    ;;
  *)
    echo "Uso: $0 dump | sql <STAGE> <MATCH_NUMBER_INICIAL> <QTD_JOGOS>" >&2
    exit 1
    ;;
esac
