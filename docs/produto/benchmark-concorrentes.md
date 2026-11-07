# Benchmark de produto: YourSummoner vs OP.GG, Mobalytics e Blitz.gg

Data da pesquisa: 2026-11-06. Idioma: pt-BR. Escopo: League of Legends.

## 0. Como ler

- Cada afirmação sobre concorrente traz um código `[Sn]`. A lista de URLs está na seção 8.
- "(não confirmado)" = não achei fonte pública que prove. Não significa que não exista.
- Vários sites (blitz.gg, mobalytics.gg, wombocombo, hexgate) devolveram 403 ou redirecionamento ao fetch automático. Parte das fontes é blog de terceiros, e alguns são de concorrentes. O peso de cada uma está na seção 8.
- Os paths dos endpoints da Riot não foram conferidos. A página [S12] confirma só os nomes das APIs. A seção 3 cita só os nomes.
- O rascunho anterior (`YourSummoner-API-quarkus/_research/benchmark-concorrentes.md`) tinha erros. Dizia que estimar MMR é permitido (a política proíbe, ver seção 4). Também marcava OP.GG sem coaching e Mobalytics com leaderboards, sem fonte.

## 1. Funcionalidades por concorrente

### OP.GG
| Grupo | O que oferece | Fonte |
|---|---|---|
| Perfil | Rank, taxa de vitória, campeões mais jogados, desempenho por campeão | [S3] [S5] |
| Partida | Histórico detalhado (builds, runas, dano, ouro). Pós-jogo com gráficos de ouro, dano, mapa de wards e linha do tempo de objetivos | [S3] [S6] |
| Campeões | Win/pick/ban, builds, runas, ordem de skills, matchups, filtro por elo e região | [S3] |
| Ao vivo | Live game com rank e taxa de vitória dos jogadores. Multi-busca de até 5 nomes | [S3] [S6] |
| Desktop | Overlays, timers de selva e objetivos, runas. Notas de versão citam chatbot de IA e histórico de augments do ARAM | [S3] [S7] |
| Rankings | Leaderboards por fila, campeão, nível, maestria e skins | [S4] |
| Patch e meta | Menu de notas de patch e "stats by tier" | [S4] |
| Modos | ARAM Mayhem, Arena, URF, Brawl, Doom Bots, Nexus Blitz | [S4] |
| Esports e social | Produtos relacionados: Esports, Duo, Streamer Overlay. Spectate de pros | [S4] |
| Preço | Grátis com anúncios. Opções pagas tiram anúncios | [S3] |

### Mobalytics
| Grupo | O que oferece | Fonte |
|---|---|---|
| Perfil | Runas e builds, calendário de atividade, recordes. Guarda todas as partidas (a afirmação de que o OP.GG apaga após o patch é do próprio Mobalytics) | [S8] |
| Coaching | GPI: nota de 8 habilidades (agressão, consistência, farm, luta, sobrevivência, trabalho em equipe, versatilidade, visão). Radar contra a média do elo. Progresso ao longo do tempo | [S8] [S6] |
| Pós-jogo | Relatório que ranqueia os 10 jogadores. Rastreio de PdL | [S9] |
| Pré/ao vivo | Pré-jogo na seleção de campeões: builds, scouting, matchup. Overlay "Live Companion" com cooldowns | [S8] |
| Campeões | Tier list, matchups, power spikes curados por especialistas, dificuldade do campeão | [S8] |
| Preço e app | Plus US$ 9,99/mês ou US$ 69,99/ano. App em Overwolf, só Windows. Plano grátis com anúncios | [S9] |
| Leaderboards, live game público, multi-busca, esports | (não confirmado) | |

### Blitz.gg
| Grupo | O que oferece | Fonte |
|---|---|---|
| Runas e builds | Importação automática por situação de lane | [S10] |
| Overlays | Lembrete de trinket, timers de minimapa, ultimates, ordem de skills, valor de itens, saúde no ARAM | [S10] [S11] |
| Augments | Overlays de augments para ARAM Mayhem e Arena | [S9] [S11] |
| Pós-partida | Insights após a partida | [S10] |
| Tier list | Página de tier list de campeões | [S10] |
| Preço e app | App Electron (Windows/macOS). Premium a partir de US$ 3,99/mês. Críticas de RAM (relatos, não medição) | [S9] |
| Perfil público, live game, leaderboards, multi-busca, esports | (não confirmado) | |

### Referência secundária
| Site | Destaque | Fonte |
|---|---|---|
| U.GG | Builds e tier list atualizados em horas após o patch. Pós-jogo fraco | [S6] |
| Porofessor | Overlay grátis com runas, timers de feitiço inimigos e dicas de lane. Site básico | [S6] |
| League of Graphs | Estatísticas globais e por servidor, tendências de meta | [S6] |

## 2. Tabela comparativa

Legenda: S = sim, N = não, P = parcial, ? = (não confirmado).
A coluna YS descreve o YourSummoner hoje (README e roadmap do repositório).

| Funcionalidade | OP.GG | Mobalytics | Blitz | YS hoje |
|---|---|---|---|---|
| Perfil com rank solo/flex | S | S | ? | S |
| Maestria por campeão | P (só leaderboard) | ? | ? | S |
| Histórico de partidas | S | S | P | S |
| Detalhe de partida | S | S | S | S (incl. Arena) |
| Gráficos e timeline da partida | S | ? | P | N |
| Estatísticas do jogador por campeão | S | S | ? | P (só maestria) |
| Tier list / win rate por campeão | S | S | S | P (pick rate por lane do Community Dragon) |
| Builds e runas | S | S | S | N (F4) |
| Ordem de skills | S | ? | S | N |
| Matchups e counters | S | S | P | N |
| Live game | S | P (pré-jogo) | ? | N |
| Multi-busca | S | ? | ? | N |
| Overlay e app desktop | S | S | S | N (fora do escopo de API) |
| Leaderboards | S | ? | ? | N |
| Notas de patch | S | ? | ? | P (lista de patches, diff em F3) |
| Coaching / GPI | P (chatbot IA) | S | P | N |
| Rastreio de PdL | ? | S | ? | N |
| Arena / ARAM augments | S | ? | S | P (detalhe Arena) |
| Esports | S | ? | ? | N |
| Catálogo de campeões, skins e dicas | S | S | ? | S |

## 3. Fonte de dados e persistência

Decisão atual: não guardar dados de jogador. O catálogo do Data Dragon no MongoDB (F1) não é dado de jogador e continua valendo.
Os nomes das APIs vêm de [S12]. Os paths exatos não foram conferidos; consulte a referência em https://developer.riotgames.com/apis.

| Funcionalidade | Fonte | APIs | Persiste partidas de muitos jogadores? |
|---|---|---|---|
| Perfil, rank, maestria | Riot | `account-v1`, `summoner-v4`, `league-v4`, `champion-mastery-v4` | Não |
| Histórico e detalhe | Riot | `match-v5` (ids e partida) | Não |
| Timeline, ordem de skills e itens da partida | Riot | `match-v5` timeline | Não, para uma partida |
| Live game | Riot | `spectator-v5` + `league-v4` + `champion-mastery-v4` | Não |
| Multi-busca | Riot | mesmos do perfil, em lote | Não |
| Leaderboards | Riot | `league-v4` (Challenger, Grandmaster, Master), `league-exp-v4`; `account-v1` por PUUID para o Riot ID | Não (cache) |
| Desafios | Riot | `lol-challenges-v1` | Não |
| Status do servidor | Riot | `lol-status-v4` | Não |
| Clash | Riot | `clash-v1` | Não |
| Itens, runas, feitiços, campeões | Data Dragon | `versions.json`, `champion.json`, `item.json`, `summoner.json` [S12] | Não |
| Pick rate por lane | Community Dragon | já em uso | Não |
| Diff de patch | Data Dragon (F3) | catálogo por versão | Não |
| Resumo pessoal (mini-GPI) | Riot | `match-v5` dos últimos N jogos do próprio jogador | Não |
| Tier list, win rate, matchups, builds, skill order de meta | Agregação própria | `match-v5`; `league-exp-v4` para amostrar jogadores | **Sim: exige rever a decisão** |
| GPI com baseline por elo | Agregação própria | | **Sim** |
| Histórico de PdL do jogador | Snapshots de `league-v4` | | **Sim (dado do jogador, opt-in)** |
| Overlay in-game | League Client / Live Client | A Riot diz que a League Client API não é suportada para terceiros [S12] | Não, mas fora do escopo de API |

Resumo:

- Cabe sem persistir: perfil, partida (incluindo timeline), live game, multi-busca, leaderboards, desafios, status, Clash, mini-GPI e diff de patch.
- Exige rever a decisão: tier list, matchups, builds de meta, GPI com baseline e histórico de PdL.
- Caminho do meio: guardar só contadores agregados anônimos (sem PUUID e sem matchId). Se isso conta como "Game Information" para a retenção da Riot: (não confirmado).

## 4. Restrições das políticas da Riot

| Tema | O que a política diz (resumo) | Impacto | Fonte |
|---|---|---|---|
| MMR oculto | A API não expõe MMR/ELO. Produtos não podem criar alternativas ao ranqueamento oficial, e calculadoras de MMR/ELO são citadas como proibidas | Não prometer "MMR estimado". O rascunho antigo errou nisso | [S1] [S12] |
| De-anonimização | Proibido de-anonimizar jogadores que não se identificam pelas informações visíveis. Proibido identificar jogadores deliberadamente ocultos pelo jogo | Sem "tag de pro/streamer" por inferência. Cuidado em live game e leaderboards | [S1] [S12] |
| Riot ID | Referência oficial é gameName+tagLine. Endpoints por nome estão descontinuados. A interface deve exibir o Riot ID | O projeto já usa Riot ID. Leaderboards precisam de `account-v1` por PUUID | [S12] |
| Dados que dão vantagem | Proibido mostrar winrate de augments ou itens do Arena. Proibido mostrar informação da sessão que o jogador ainda não conhecia | Tier list de Arena fica fora. Live game: validar o enquadramento com a Riot (não confirmado se o spectator padrão se enquadra) | [S12] |
| Chave pessoal | Para uso próprio ou comunidade pequena e privada. Sem aumento de limite. Não pode servir ao público, nem em alfa/beta aberto | O YourSummoner público precisa de chave de produção | [S2] |
| Chave de produção | Exige registro do produto e protótipo funcional. Uma chave por produto. Chave protegida, nunca no código | Registrar o produto antes de abrir ao público | [S1] [S2] |
| Rate limits | Por região. Pessoal: 20 req/s e 100 req/2 min. Produção inicial: 500 req/10 s e 30.000 req/10 min. Há limite de aplicação, de método e de serviço. Em 429, obedecer `Retry-After` | Fan-out de live game, multi-busca e leaderboards precisa de F5 | [S2] |
| Divergência de limites | Os termos de uso citam 10 chamadas/10 s para a dev key [S14]. O portal cita 20 req/s para chave pessoal [S2] | Tratar o menor valor como seguro. Qual vale hoje: (não confirmado) | [S14] [S2] |
| Retenção | Ao encerrar os termos, apagar a Game Information em posse. A Riot repassa pedidos de exclusão (GDPR) aos desenvolvedores por lista de identificadores. Prazo máximo de retenção: (não confirmado) | Persistir dados de jogador exige processo de exclusão por PUUID | [S14] |
| Monetização | Precisa de nível gratuito (pode ter anúncios). Sem anúncios em propriedades da Riot. Cobrar acesso à Game Information exige aprovação prévia por escrito. Sem apostas ou cripto | Plano pago só com aprovação da Riot | [S1] [S14] |
| Marca | Não sugerir endosso da Riot (Riot Marks) | Texto legal "não endossado pela Riot" no front | [S14] |
| Revenda | Proibido revender ou sublicenciar o acesso à API | Não expor a API como serviço para terceiros | [S14] |
| Overlay | League Client API não é suportada para terceiros | Overlay desktop é risco; manter fora do escopo | [S12] |

A política geral informa atualização em 29/05/2025 [S1].

## 5. Propostas de entregas futuras (F7 em diante)

F1 a F6 já existem. Ordem: valor/esforço, do melhor para o pior.
Esforço: P = pequeno (até 2 dias), M = médio (3 a 7 dias), G = grande (semanas). A estimativa é minha, sem medição.
"Persiste?" refere-se a partidas ou dados de muitos jogadores.

| Fase | Proposta | O que entrega ao usuário | Fontes de dados | Persiste? | Esforço | Depende de | Por que vale |
|---|---|---|---|---|---|---|---|
| F7 | Multi-busca | Colar até 5 Riot IDs e ver rank, top maestrias e vitórias recentes de cada um | `account-v1`, `league-v4`, `champion-mastery-v4`, `match-v5` | Não | P | perfil existente | OP.GG tem multi-busca de até 5 nomes [S3] [S6]. Reaproveita quase tudo |
| F8 | Estatísticas do jogador por campeão | Por campeão: partidas, vitórias, KDA, farm e itens finais mais usados nas últimas N partidas | `match-v5`, `champion-mastery-v4` | Não | P | perfil e histórico existentes | OP.GG mostra desempenho por campeão [S3]. Entrega "builds" sem agregar a base |
| F9 | Partida ao vivo | Os jogadores da partida atual com rank, maestria do campeão escolhido e taxa de vitória | `spectator-v5`, `league-v4`, `champion-mastery-v4`, Data Dragon | Não | M | F5 recomendada (10 a 25 chamadas por consulta) | OP.GG tem live game com rank e taxa de vitória [S3] [S6]. Mobalytics tem scouting pré-jogo [S8]. Ver restrição de informação oculta (seção 4) |
| F10 | Timeline da partida | Gráfico de ouro/XP por minuto, ordem de skills, ordem de itens e linha de objetivos | `match-v5` timeline | Não | M | detalhe de partida; F4 para nomes de itens | OP.GG tem gráficos de ouro, wards e objetivos [S6]. Blitz mostra ordem de skills [S11] |
| F11 | Leaderboards | Ranking por região e fila (Challenger, Grandmaster, Master) com Riot ID | `league-v4`, `league-exp-v4`, `account-v1` | Não (cache) | M | F5 (muitas chamadas de `account-v1`) | OP.GG tem leaderboards por fila, campeão, maestria e skins [S4]. Cuidado com a de-anonimização |
| F12 | Painel de mudanças do patch | Tela de buffs e nerfs por campeão, item e runa entre dois patches | Data Dragon | Não | M | F3, F4 | OP.GG tem patch notes e stats por elo [S4]. Usa o que já planejamos |
| F13 | Mini-GPI pessoal | Nota do jogador em farm, visão, luta e participação, comparada à própria média | `match-v5` dos últimos N jogos | Não | M | F8; F10 opcional | Mobalytics tem o GPI com 8 habilidades [S8] [S6]. Sem baseline de elo |
| F14 | Desafios | Pontos, categorias, progresso e percentis de desafios do jogador | `lol-challenges-v1` | Não | P | F2 para nomes localizados | Concorrente com isso: (não confirmado). Dado novo, esforço baixo |
| F15 | Status e Clash | Aviso de manutenção por região, agenda e times de Clash | `lol-status-v4`, `clash-v1` | Não | P | nenhuma | Concorrente com isso: (não confirmado). Complementar |
| F16 | Histórico de PdL opt-in | Curva de PdL do jogador ao longo do tempo | `league-v4` em snapshots periódicos | **Sim** (rank do jogador) | M | rever a decisão; exclusão por PUUID | Mobalytics rastreia PdL [S9]. Exige consentimento |
| F17 | Agregação de meta | Contadores anônimos de win, pick e ban por campeão, elo e patch, a partir de amostra de partidas | `match-v5`; `league-exp-v4` para amostrar | **Sim** (agregados anônimos) | G | rever a decisão; F3, F5; chave de produção | É a base do que todos têm: tier list, builds e matchups [S3] [S6] [S10]. Não expor winrate de Arena (seção 4) |
| F18 | Tier list, builds, matchups e skill order de meta | Telas de meta por role, elo e patch | camada sobre F17 | Não (lê agregados) | M | F17, F4 | Tier list é o núcleo de OP.GG, Mobalytics e Blitz [S3] [S8] [S10]. U.GG compete por velocidade de atualização [S6] |
| F19 | GPI com baseline por elo | Radar do jogador contra a média do elo dele | agregados de F17 + F13 | Sim (agregados) | M | F13, F17 | É o diferencial do Mobalytics [S6] |

### Próximo passo recomendado para amanhã

As três primeiras: **F7 (multi-busca)**, **F8 (estatísticas por campeão)** e **F9 (partida ao vivo)**.

| Critério | Motivo |
|---|---|
| Persistência | Nenhuma exige rever a decisão atual |
| Reuso | F7 e F8 usam quase só adapters que já existem |
| Valor | Live game e multi-busca são as ferramentas de scouting que o OP.GG destaca [S3] [S6] |
| Risco | F9 faz várias chamadas por consulta. Rodar com cache e propagar o 429 enquanto F5 não chega |
| Pendência | Validar a restrição de "informação da sessão desconhecida" antes de publicar F9 (seção 4) |

F17 deve ser decidida à parte. É o item mais valioso e o único que muda a política de dados do produto. Antes dela, registrar o produto e pedir a chave de produção.

## 6. Decisões em aberto

| Decisão | Opções |
|---|---|
| Persistência de dados de jogador | Manter "não guardar" até F15. Depois, abrir exceção para agregados anônimos (F17) e snapshots opt-in (F16) |
| Chave de produção | Registrar antes de abrir ao público ou de rodar F17 [S2] |
| Overlay desktop | Fora do escopo. A League Client API não é suportada [S12] |
| Monetização | Fica gratuito. Cobrar exige aprovação escrita da Riot [S14] |

## 7. Limites desta pesquisa

- Sites dos concorrentes bloquearam parte dos fetches. Usei buscas, menus públicos (op.gg) e reviews de terceiros.
- As afirmações sobre Blitz e Mobalytics vêm de reviews e resultados de busca. Alguns são de concorrentes (Hexgate) ou do próprio Mobalytics.
- Os preços são os dos reviews, não conferidos nos sites oficiais.
- Os paths dos endpoints da Riot não foram conferidos.

## 8. Fontes

| Código | URL | Tipo e peso |
|---|---|---|
| S1 | https://developer.riotgames.com/policies/general | Oficial (Riot) |
| S2 | https://developer.riotgames.com/docs/portal | Oficial (Riot) |
| S3 | https://lolnow.gg/op-gg/ | Review de terceiros |
| S4 | https://www.op.gg/ | Oficial (menus públicos do OP.GG) |
| S5 | https://opgg.helpscoutdocs.com/category/6-summoner-page | Oficial (ajuda OP.GG); só o título apareceu na busca, a página deu 404 no fetch |
| S6 | https://agatasmurf.com/best-lol-stats-sites/ | Blog de terceiros (serviço de contas, interesse comercial) |
| S7 | https://op.gg/desktop/fi/patch-notes | Oficial (notas do OP.GG Desktop) |
| S8 | https://mobalytics.gg/opgg-vs-mobalytics/ | Página do próprio Mobalytics, comparando com o OP.GG; fetch deu 403, uso o resultado da busca |
| S9 | https://hexgate.pages.dev/blog/hexgate-vs-blitz-vs-mobalytics/ | Blog de concorrente (Hexgate) |
| S10 | https://blitz.gg/lol | Oficial; fetch deu 403, uso o resultado da busca |
| S11 | https://blitz.gg/lol/overlays | Oficial; fetch deu 403, uso o resultado da busca |
| S12 | https://developer.riotgames.com/docs/lol e https://developer.riotgames.com/apis | Oficial (Riot) |
| S14 | https://developer.riotgames.com/terms | Oficial (Riot) |
