# Data Dragon e Community Dragon

## Data Dragon (CDN Público)
Serviço estático oficial da Riot Games para dados de campeões, itens, runas, feitiços e assets visuais. **Não requer API Key**.

### URLs Base e Endpoints Utilizados
- **Lista de versões:** `https://ddragon.leagueoflegends.com/api/versions.json`
- **Lista de campeões por patch e locale:** `https://ddragon.leagueoflegends.com/cdn/{version}/data/{locale}/champion.json`
- **Detalhes de um campeão:** `https://ddragon.leagueoflegends.com/cdn/{version}/data/{locale}/champion/{championId}.json`
- **Ícone de perfil do invocador:** `https://ddragon.leagueoflegends.com/cdn/{version}/img/profileicon/{profileIconId}.png`
- **Ícone de campeão:** `https://ddragon.leagueoflegends.com/cdn/{version}/img/champion/{championId}.png`
- **Splash Art de skin:** `https://ddragon.leagueoflegends.com/cdn/img/champion/splash/{championId}_{num}.jpg`
- **Loading Screen de skin:** `https://ddragon.leagueoflegends.com/cdn/img/champion/loading/{championId}_{num}.jpg`

### Locales
A API utiliza atualmente `pt_BR` (configurável via `yoursummoner.locale`). A estrutura suporta múltiplos idiomas por patch no armazenamento.

### Skins e Chromas
- O Data Dragon lista todas as skins de um campeão, incluindo cromas e subvariantes.
- A API filtra e remove da listagem as chromas (`parentSkin != null`), mantendo apenas as skins principais (`parentSkin == null`).

---

## Community Dragon (CDragon)
Repositório mantido pela comunidade com arquivos extraídos diretamente do cliente do jogo League of Legends.

### Endpoint de Estatísticas
- **Pick rate global por lane:** `https://raw.communitydragon.org/latest/plugins/rcp-fe-lol-champion-statistics/global/default/rcp-fe-lol-champion-statistics.js`
- **Formato:** O arquivo é um script JavaScript (`JSON.parse('...')`) que embute o dicionário consolidado de taxas de escolha por campeão e lane.
- **Disponibilidade:** O CDragon expõe este recurso exclusivamente no path `latest`. O sistema captura este snapshot vinculado ao patch ativo no momento da ingestão.
