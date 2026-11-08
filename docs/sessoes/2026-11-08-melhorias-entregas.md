# Melhorias nas entregas já feitas (2026-11-08)

Só o que já existe e pode ficar melhor. Ideias novas estão no `2026-11-08-panorama.md`.

## live-builds

| Entrega | O que melhorar | Por quê |
|---|---|---|
| Painel de elo alto (#1) | Itens LOW da revisão (#16): validar o pacote antes de gravar o cache, nova tentativa do patch anterior, não travar o painel com cache frio sem rede (~30 s), aviso de cache antigo do patch anterior | robustez offline e com pacote ruim |
| Painel de elo alto | Mostrar a **origem** da build em uma linha discreta ("Mestre+ BR · 16.22 · 319 partidas") | o usuário quis saber de onde veio a build do Graves |
| Seleção (LCU) | Confirmar no cliente a resposta do **limite de páginas cheio** e a escrita de **feitiços** (`my-selection`) com uma captura real; hoje só suposição/teste do usuário | dois pontos ainda sem evidência gravada |
| Seleção | Detectar outro importador ativo (página "Blitz: …" virando ativa depois da nossa) e avisar | Blitz e live-builds podem desfazer um ao outro |
| Seleção | Arsenal em gravação quando a opção de runa muda termina com a opção anterior (limite conhecido) | consistência arsenal × runa |
| Sonda (`lcu_probe`) | Não imprimir o `summonerId` na linha "consultando" | dado pessoal no console |
| Árvore com ícones (#17/#18) | Testar no cliente (layout novo e Precisão corrigida ainda não testados pelo usuário) | entregue sem teste real |
| Painel da partida (#19) | Testar numa partida real e medir CPU/RAM durante o jogo (`measure.ps1`); última medição em jogo é da v1 (~94 MB) | ícones e grade podem pesar |
| Posse de itens | Item de grau superior (#14) e item completo pelo `into` (#2) | marcar etapas corretamente em builds com Lágrima/Brilho/Tomo |
| Distribuição | CI gerar o `live-builds.exe` como artefato (e release) | facilitar entregar o app às pessoas selecionadas |

## yoursummoner_processor

| O que melhorar | Por quê |
|---|---|
| Amostra dos itens 4–6 (1–4 partidas por página): mostrar e ordenar por confiança; combinar com a coleta de mais servidores | recomendações longas instáveis |
| Recalcular só o patch que recebeu partidas novas (hoje recalcula tudo a cada hora; 16.21 levou 37 s com 6,5 mil partidas) | custo cresce com a coleta de todos os servidores |
| Dimensão de região/elo no pacote quando a coleta abrir para outros servidores | BR pode divergir do meta mundial (caso Graves) |
| `skillOrders` só usa sequências de 9 pontos; partidas curtas ficam de fora | grade de skills com amostra maior |
| LOW da revisão (#7) | dívida pequena acumulada |

## yoursummoner_collector

| O que melhorar | Por quê |
|---|---|
| **Backup diário do Mongo** (`mongodump` + rotação) | partidas são o ativo do projeto e não há cópia |
| Coletar `timeCCingOthers` e `totalHeal` | pré-requisito do motor |
| Continuar coletando com o PC desligado (mini PC) e retomar sozinho depois de queda de luz (`integrity-check -Fix` agendado no logon) | coleta parou várias vezes hoje |
| Partidas v1 (221 documentos ignorados pelo processor): migrar ou apagar | ruído no banco |
| Teto de 15 mil por patch (#5/#6) com vários servidores | o teto foi pensado para um servidor |

## YourSummoner-API

| O que melhorar | Por quê |
|---|---|
| Admin: QR code do TOTP (#3) e impedir reuso do código (#4) | segurança e usabilidade do admin |
| Deadline total por chamada externa (#5) e cache do Data Dragon com Mongo fora (#6) | resiliência já mapeada |

## Scripts e processo

| O que melhorar | Por quê |
|---|---|
| Versionar `D:\Workspace\Scripts\yoursummoner` | hoje sem histórico nem revisão |
| `board.ps1` testado contra o GitHub real (só o autoteste local roda) | bug de acento só apareceu em uso real |
| Revisões: incluir no prompt o "Review Focus" da task e os achados já descartados | menos falsos positivos (ex.: `fs::rename` no Windows) |
| Specs com fixtures reais desde o início (como o Graves) | os bugs desta rodada vieram de dado real não coberto |
