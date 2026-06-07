#!/usr/bin/env python3
"""
Gera V30__seed_players.sql com os jogadores convocados para a Copa 2026.
Fonte: Wikipedia — 2026 FIFA World Cup squads

Dependências: pip install requests beautifulsoup4
Uso: python3 scripts/seed-players.py
"""

import re
import sys
import requests
from bs4 import BeautifulSoup

# Mapeamento: nome exato do <h3> na Wikipedia → código FIFA (tabela teams no banco)
TEAM_CODE_MAP = {
    'Mexico': 'MEX',
    'South Africa': 'RSA',
    'South Korea': 'KOR',
    'Czech Republic': 'CZE',
    'Canada': 'CAN',
    'Bosnia and Herzegovina': 'BIH',
    'Switzerland': 'SUI',
    'Qatar': 'QAT',
    'Brazil': 'BRA',
    'Morocco': 'MAR',
    'Scotland': 'SCO',
    'Haiti': 'HAI',
    'United States': 'USA',
    'Paraguay': 'PAR',
    'Australia': 'AUS',
    'Turkey': 'TUR',
    'Germany': 'GER',
    'Curaçao': 'CUW',
    'Ivory Coast': 'CIV',
    'Ecuador': 'ECU',
    'Netherlands': 'NED',
    'Japan': 'JPN',
    'Tunisia': 'TUN',
    'Sweden': 'SWE',
    'Belgium': 'BEL',
    'Egypt': 'EGY',
    'Iran': 'IRN',
    'New Zealand': 'NZL',
    'Spain': 'ESP',
    'Cape Verde': 'CPV',
    'Saudi Arabia': 'KSA',
    'Uruguay': 'URU',
    'France': 'FRA',
    'Senegal': 'SEN',
    'Norway': 'NOR',
    'Iraq': 'IRQ',
    'Argentina': 'ARG',
    'Algeria': 'ALG',
    'Austria': 'AUT',
    'Jordan': 'JOR',
    'Portugal': 'POR',
    'DR Congo': 'COD',
    'Colombia': 'COL',
    'Uzbekistan': 'UZB',
    'England': 'ENG',
    'Croatia': 'CRO',
    'Ghana': 'GHA',
    'Panama': 'PAN',
}

URL = 'https://en.wikipedia.org/wiki/2026_FIFA_World_Cup_squads'

# h3 não-país para ignorar
IGNORE_SECTIONS = {
    'Age', 'Player representation by club', 'Player representation by league system',
    'Player representation by club confederation', 'Average age of squads',
    'Coach representation by country',
}


def clean_name(name: str) -> str:
    """Remove referências Wiki, anotações e espaços extras."""
    name = re.sub(r'\[.*?\]', '', name)     # [nota]
    name = re.sub(r'\(captain\)', '', name, flags=re.IGNORECASE)
    name = name.strip()
    return name


def escape_sql(s: str) -> str:
    return s.replace("'", "''")


def fetch_squads() -> dict[str, list[str]]:
    """Retorna {team_code: [player_name, ...]}"""
    print(f'Baixando {URL} ...', file=sys.stderr)
    r = requests.get(URL, headers={'User-Agent': 'Mozilla/5.0'}, timeout=30)
    r.raise_for_status()
    soup = BeautifulSoup(r.text, 'html.parser')

    squads: dict[str, list[str]] = {}

    for h3 in soup.find_all('h3'):
        section_title = h3.get_text(strip=True)

        if section_title in IGNORE_SECTIONS:
            continue

        code = TEAM_CODE_MAP.get(section_title)
        if not code:
            print(f'  AVISO: seção ignorada: {repr(section_title)}', file=sys.stderr)
            continue

        # Próxima tabela após o h3
        table = h3.find_next('table')
        if not table:
            print(f'  AVISO: tabela não encontrada para {section_title}', file=sys.stderr)
            continue

        players = []
        for row in table.find_all('tr'):
            cells = row.find_all(['td', 'th'])
            if len(cells) < 3:
                continue
            if cells[0].name == 'th':  # linha de cabeçalho
                continue
            name = clean_name(cells[2].get_text())
            if name:
                players.append(name)

        squads[code] = players
        print(f'  {code}: {len(players)} jogadores', file=sys.stderr)

    return squads


def generate_sql(squads: dict[str, list[str]]) -> str:
    lines = [
        '-- Gerado por scripts/seed-players.py',
        '-- Fonte: https://en.wikipedia.org/wiki/2026_FIFA_World_Cup_squads',
        '',
        'DELETE FROM players;',
        '',
    ]

    total = 0
    for code in sorted(squads.keys()):
        players = squads[code]
        if not players:
            continue
        lines.append(f'-- {code}')
        for name in players:
            safe = escape_sql(name)
            lines.append(
                f"INSERT INTO players (name, team_id) "
                f"SELECT '{safe}', id FROM teams WHERE code = '{code}';"
            )
        total += len(players)
        lines.append('')

    lines.append(f'-- Total: {total} jogadores em {len(squads)} seleções')
    return '\n'.join(lines)


def main():
    squads = fetch_squads()

    if not squads:
        print('ERRO: nenhum elenco encontrado.', file=sys.stderr)
        sys.exit(1)

    total = sum(len(v) for v in squads.values())
    print(f'\nTotal: {total} jogadores em {len(squads)} seleções', file=sys.stderr)

    missing = set(TEAM_CODE_MAP.values()) - set(squads.keys())
    if missing:
        print(f'AVISO: seleções faltando: {sorted(missing)}', file=sys.stderr)

    sql = generate_sql(squads)

    out_path = 'src/main/resources/db/migration/V30__seed_players.sql'
    with open(out_path, 'w', encoding='utf-8') as f:
        f.write(sql)

    print(f'Gerado: {out_path}', file=sys.stderr)


if __name__ == '__main__':
    main()
