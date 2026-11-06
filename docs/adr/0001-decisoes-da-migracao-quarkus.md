# ADR 0001 — Decisões da migração Spring Boot → Quarkus

- Status: aceita
- Data: 2026-10-22
- Branch: `feature/quarkus-hexagonal`

## Contexto

A API saiu de Spring Boot (Java 11) para Quarkus 3.40.1 em Java 25, com arquitetura hexagonal, consumindo a Riot Games API, o Data Dragon e o Community Dragon. A Riot tem um host por plataforma/região, aplica rate limit, e as dev keys expiram a cada 24h. Este ADR registra as decisões que não são evidentes só pelo código.

## Decisões

### (a) Clients da Riot por plataforma/região

Os clients REST da Riot são construídos com `QuarkusRestClientBuilder` e cacheados por par (interface, host). Como cada plataforma/região tem seu próprio host, um client com URL fixa não serviria.

### (b) Fault Tolerance nos adapters

A anotação de Fault Tolerance fica no nível da classe dos adapters de saída. O retry só acontece em falha transitória. O circuit breaker conta apenas indisponibilidade e timeout.

O circuit breaker é um por método e é compartilhado entre regiões. Essa limitação foi aceita por enquanto: uma região com problema pode afetar as demais.

*Revisão de 2026-10-22, após auditoria:*
- O circuit breaker usa `failOn = {ExternalServiceUnavailableException, TimeoutException}`. Antes, 401/403/400 e a chave ausente também abriam o circuito, transformando o 502 em 503.
- O `@Timeout` da FT é de 7s. Esse valor fica acima de 2s de conexão + 4s de leitura do client, então o timeout do client, que é retentável, dispara primeiro.
- O Data Dragon e o Community Dragon usam a mesma regra: `@Timeout` de 12s, contra os 2s de conexão + 8s de leitura do client. No Data Dragon, 403 e 404 em `latestVersion()`/`champions()` viram 503, que é retentável; isso cobre o dia de patch, quando o `versions.json` já cita a versão nova e o CDN ainda não a publicou. O `DataDragonAdapter` guarda o último patch e o último catálogo que deram certo e os devolve, com WARN, quando o refresh falha. Limite aceito: o catálogo guardado pode ser de um patch anterior ao da versão guardada.
- As anotações de FT continuam repetidas nos 5 adapters da Riot, de propósito. Um `@Stereotype` não tem suporte confirmado no SmallRye FT e quebraria as chaves de config por classe (`<Adapter>/CircuitBreaker/...`) que os testes usam. Um `@ApplyGuard` compartilhado juntaria os 5 circuit breakers em um, e a falha de um endpoint passaria a cortar os outros. Reavaliar se o SmallRye FT passar a suportar estereótipo com config por classe.

### (c) Cache Caffeine nos adapters de saída

Os adapters de saída usam Caffeine (`quarkus-cache`), com TTL definido por tipo de dado (ver `application.properties`). O resultado "não encontrado" não é colocado no cache.

*Revisão de 2026-10-22:* o `@CacheResult` cacheava o `Optional.empty()` devolvido para o 404. Os adapters da Riot e o `DataDragonCatalog.champion()` passaram a usar cache programático (`CachedCalls`): o loader lança `UpstreamNotFoundException`, nada entra no cache e o adapter converte em vazio fora dele. A chave do account usa `gameName` e `tagLine` em minúsculas, porque o Riot ID não diferencia caixa.

### (d) Sem rate limiter próprio

O 429 da Riot é propagado ao cliente com o header `Retry-After`. A mitigação é o cache. Um limitador por host fica como próximo passo, caso surja tráfego concorrente que justifique.

### (e) Erros RFC 9457

Os erros são respondidos como `application/problem+json` via `quarkus-http-problem`. `include-details` fica `false`, então não há stack trace na resposta.

### (f) Chamadas do perfil em sequência

As chamadas do perfil são feitas em sequência. Fan-out paralelo só será adotado se a latência medida justificar.

*Revisão de 2026-10-23, histórico de partidas:* `GET /matchs/{nick}/{tag}/history` faz 1 chamada de ids e até `quantity` de detalhe. Em sequência isso levaria segundos, então os detalhes são buscados em paralelo, em virtual threads, com no máximo 4 chamadas simultâneas: a dev key da Riot permite 20 req/s. O cache `riot-match` (24h) torna as repetições baratas. O `quantity` vai de 1 a 100, que é o limite do endpoint de ids da Riot, e o padrão é 20. Limite aceito: com dev key (100 req/2 min), um histórico grande sem cache esgota a cota da janela e as chamadas seguintes recebem 429. Com chave de produção, ou com o cache `riot-match` aquecido, isso não acontece. Uma partida que some (404) entre a lista e o detalhe é pulada. 429 e 503 derrubam a requisição inteira, coerente com (d). O perfil continua em sequência.

### (g) Chave da Riot por variável de ambiente

A chave vem somente de `RIOT_API_KEY`. Ela é opcional na subida: se ausente, a aplicação sobe e registra um WARN no log. Sem chave, as chamadas à Riot respondem 502.

*Revisão de 2026-10-24:* a propriedade interna é `riot.api.token=${RIOT_API_KEY:}`, e não `riot.api.key`. O Quarkus lê o `.env` como variável de ambiente (ordinal 300), e `RIOT_API_KEY` é o nome canônico de `riot.api.key`. Com esse nome, a chave real vence o `%test.` (ordinal 250) e vaza para os testes. Isso aconteceu: com um `.env` local, 7 testes enviaram a chave real ao WireMock.

### (h) Endpoint de lane com semântica alterada

A Riot removeu o campo `chestGranted`. Por isso, o endpoint de lane mudou de semântica em relação à versão Spring.

### (i) Sem OpenTelemetry por enquanto

Não adicionamos `quarkus-opentelemetry` para não exigir um collector no ambiente. Quando houver um collector, a extensão deve ser adicionada.

### (j) Chaves antigas no histórico git

As chaves que aparecem no histórico git são dev keys da Riot já expiradas. O histórico não foi reescrito.

## Consequências

- A troca de região não gera client novo a cada chamada, e o cache por host evita reconstruções.
- O circuit breaker compartilhado pode abrir por falha em uma região e afetar as outras até se recuperar.
- Sem rate limiter próprio, o tráfego concorrente depende do cache e do `Retry-After` repassado ao cliente.
- Sem chave configurada, a API sobe, mas todas as rotas que dependem da Riot respondem 502 até a variável ser definida.
- Os consumidores do endpoint de lane precisam considerar a mudança de semântica decorrente da remoção de `chestGranted`.
- Sem OpenTelemetry, não há traces distribuídos até que o collector exista.
