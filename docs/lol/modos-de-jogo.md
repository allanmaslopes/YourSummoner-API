# Modos de Jogo e Filas no League of Legends

## Modo Arena (`gameMode = CHERRY`)
- **Dinâmica:** Partidas em formato de duplas (2v2v2v2), totalizando 8 equipes e 16 jogadores (confirmar).
- **Campos na API:** O payload de partida (`match-v5`) utiliza campos específicos para este modo:
  - `playerSubteamId`: Identificador da subequipe/dupla.
  - `subteamPlacement`: Colocação final da dupla na partida (ex: 1º ao 4º lugar).

## Principais Filas (Queue IDs)
As filas mais comuns consultadas na API através do filtro `queue` são:

| Queue ID | Nome da Fila | Descrição |
|---|---|---|
| `420` | Ranqueada Solo/Duo | Fila ranqueada individual ou em duplas. |
| `440` | Ranqueada Flex | Fila ranqueada em grupos de 1, 2, 3 ou 5 jogadores. |
| `450` | ARAM (All Random All Mid) | Modo aleatório no Abismo Uivanante. |
| `400` / `430` | Normal Game | Partidas normais (Draft Pick / Blind Pick) (confirmar). |

## Impacto na API
- Os endpoints de histórico e listagem de IDs de partida (`/matchs`) suportam filtragem opcional por ID de fila (`queue`), permitindo segmentar estatísticas e exibições por modo de jogo.
- Participantes e estatísticas por rota (`teamPosition`) comportam-se de forma diferente em modos como ARAM (onde não há rota fixa) ou Arena, sendo tratados defensivamente pelos mappers e serviços.
