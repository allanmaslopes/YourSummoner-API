# Glossário do YourSummoner

Termos usados nas conversas e nos designs do ecossistema YourSummoner (API, coletor, processor, live-builds). Atualizado em 2026-11-07. Quando um termo vira regra de código, a regra está no design citado.

## League of Legends (jogo)

| Termo | O que é | Onde importa |
|---|---|---|
| Patch | Versão do jogo (`16.22`), em geral nova a cada 2 semanas às quartas, com hotfixes fora do calendário. | Tudo é versionado por patch. Ver `patches-e-versoes.md`. |
| Rota / posição | `TOP`, `JUNGLE`, `MIDDLE`, `BOTTOM` (atirador/ADC), `UTILITY` (suporte). | Estatísticas por campeão **e** rota. |
| Elo / tier | Ferro … Mestre, Grão-Mestre, Desafiante. "Mestre+" = Mestre, Grão-Mestre e Desafiante. | O coletor só aprende com Mestre+. |
| Fila 420 | Ranqueada solo/duo. | A única fila coletada hoje. |
| Arena (`CHERRY`) | Modo 2v2v2v2 com subtimes e colocação. A política da Riot restringe expor taxa de vitória do Arena. | Fase futura (issue no quadro). |
| Item inicial | Compras dos primeiros 60 s (Lâmina de Doran, poções, item de suporte/selva). Baratos (< 1000 de ouro). | Regra própria, separada da árvore ≥ 1000. |
| Item completo | Item que não vira outro (`into` vazio), ≥ 2000 de ouro, sem botas e consumíveis. | Caminho de build (3 primeiros itens). |
| Árvore de compras | Sequência de todo item ≥ 1000 de ouro (componentes caros, botas nível 2, completos). | Dizer em que gastar o ouro na volta à base. |
| Missão de rota | Missões por função que evoluem itens, ex.: Grevas do Berserker `3006` → Grevas Bélicas `3172` (`3172.from = [3006]`). O item evoluído conta como o base. | Bug live-builds#5; regra de normalização no processor. |
| Item de bola de neve | Barato e que cresce com vantagem: Selo Sombrio (acumula PdH com abates/assistências), a "abatedura" de atirador que saiu na frente. | Só recomendar quando a vantagem favorece (engine#3). |
| Passos de Mercúrio | Bota de resistência mágica e tenacidade (reduz controle de grupo). | Exemplo de escolha pelo perfil do time inimigo. |
| Antecura | Itens que reduzem cura do inimigo. | Entrada do motor situacional. |
| Controle de grupo (CC) | Atordoar, enraizar, lentidão etc. | "Time com muito CC → Mercúrio". |
| Perfil de dano | Proporção de dano físico, mágico e verdadeiro do time inimigo. | "Armadura e depois resistência mágica". |
| Confronto (matchup) | Campeão contra o oponente da mesma rota. | Taxa de vitória por confronto (processor#4). |
| Ordem de skills / prioridade | Sequência dos pontos em Q/W/E/R; prioridade = ordem de maximizar (ex.: `R > Q > E > W`). | Riot e elo alto. |
| Runas / página de runas | Estilo principal, secundário, perks e fragmentos. | Sugestão e editor no live-builds. |
| Feitiços de invocador | Flash, Teleporte, Curar etc. | Sugestão e aplicação na seleção. |
| MMR / ELO oculto | Pontuação interna. **Calculadora de MMR é proibida pela Riot.** | Nunca implementar. |

## Fontes de dados da Riot

| Termo | O que é |
|---|---|
| Riot API | API web oficial (`*.api.riotgames.com`), exige chave. |
| Plataforma × região | Plataforma = servidor (`br1`, `kr`…; summoner, league, mastery). Região = cluster (`americas`, `europe`, `asia`, `sea`; account e match). O limite de requisições é **por região**. |
| Chave de desenvolvimento | Vence a cada 24 h; 20 req/s e 100 req/2 min por região. |
| Chave pessoal | Não vence; mesmos limites; produto pessoal aprovado pela Riot. |
| Chave de produção | Limites maiores; exige produto público aprovado. |
| PUUID / Riot ID | Identificador estável do jogador / `nome#tag`. **Nunca guardados nas partidas coletadas.** |
| `league-v4` | Ligas; `challenger/grandmaster/masterleagues` dão os jogadores Mestre+. |
| `match-v5` + timeline | Partida e linha do tempo minuto a minuto (frames e eventos de compra, skill, abate). |
| `spectator-v5` | Partida em andamento (para "partida ao vivo"). |
| Data Dragon | CDN oficial com dados estáticos por patch (`item.json`, `champion.json`, imagens). |
| Community Dragon | Espelho comunitário dos arquivos do jogo. Fonte da **recomendação oficial da Riot**: `game/data/characters/<alias>/<alias>.bin.json`, entradas `ItemRecommendationOverrideSet` (itens) e `RecSpellRankUpInfoList` (ordem de skills). O `recommendedItemDefaults` vem vazio. |
| Live Client Data API | API local do **jogo** (`https://127.0.0.1:2999`), só leitura, durante a partida. Usada pelo live-builds. |
| LCU (League Client Update) | API local do **cliente** (porta e senha no `lockfile`), lê e **escreve**: runas, pick/ban, feitiços, item sets, aceitar partida. Não é oficialmente suportada. Inventário em `live-builds/docs/lcu.md`. |
| Vanguard | Anti-cheat da Riot. Proibido injetar código, ler memória ou fazer hook no jogo. |
| China | Servidores da Tencent, fora da API pública da Riot (confirmar). Coreia (KR) está disponível. |

## Arquitetura do ecossistema

| Termo | O que é |
|---|---|
| YourSummoner-API | API Spring Boot 4 hexagonal (perfil, partidas, catálogo, admin). |
| Coletor (`yoursummoner_collector`) | Coleta partidas Mestre+ anônimas por região, com orçamento de 60% da cota e teto por patch. |
| Processor (`yoursummoner_processor`) | Recalcula, por patch, estatísticas de elo alto e publica o pacote de conhecimento. |
| live-builds | App Rust/egui no 2º monitor; lê a partida e mostra estado, ouro, build e skills. |
| live-builds-engine | Futuro motor de recomendação situacional (roda no PC do jogador). |
| Contrato `matches` | Formato da partida guardada pelo coletor; `schemaVersion: 2` (situação por minuto em `frames`, eventos apontam o frame). |
| Situação / frame | Estado dos 10 jogadores num minuto: ouro, ouro total, nível, XP, CS, abates por time. |
| Amostra de decisão | "Nesta situação, este item foi comprado, e o time venceu?" — base da recomendação situacional. |
| Pacote de conhecimento | JSON por patch publicado pelo processor, baixado pelo live-builds com ETag. |
| Amostra mínima | 30 partidas por campeão e rota; 10 ocorrências por sequência. Abaixo disso, não publica. |
| Orçamento de cota | Fração do limite da Riot que o coletor pode usar (0,6), deixando o resto para o site. |
| Teto por patch | Máximo de partidas guardadas por patch e região (15 mil), por causa do disco. |
| Cofre | Segredos (chave da Riot) criptografados no Mongo com a chave mestra; tela de admin com senha + TOTP. |
| Quadro | GitHub Projects "YourSummoner": https://github.com/users/allanmaslopes/projects/2 — fonte do andamento. |
