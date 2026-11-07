# YourSummoner-API em Spring Boot 4 — Plano de implementação

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reescrever a API em Spring Boot 4 / Java 25, hexagonal, com paridade total com a versão Quarkus e com o catálogo do Data Dragon persistido no MongoDB por patch.

**Architecture:** Mesmo layout de pacotes do Quarkus (`com.your.summoner.{domain,application,infrastructure}`). O domínio é copiado quase sem mudança. A camada `application` troca CDI por `@Service`. Os adapters Quarkus REST Client/SmallRye FT viram OpenFeign/Resilience4j. Entra um catálogo estático em duas camadas: Caffeine (L1) → MongoDB (L2) → Data Dragon.

**Tech Stack:** Java 25, Spring Boot 4.x, Spring Cloud OpenFeign + CircuitBreaker Resilience4j, Spring Data MongoDB, Caffeine, Jakarta Validation, springdoc-openapi, MapStruct, Lombok, JUnit 5, Mockito, WireMock, ArchUnit, JaCoCo, flapdoodle embedded Mongo.

**Spec:** `docs/superpowers/specs/2026-11-06-spring-boot-4-hexagonal-design.md`

**Referência (somente leitura):** worktree `D:\Workspace\YourSummoner-API-quarkus` (branch local `feature/quarkus-hexagonal`). Sempre que uma tarefa disser "portar X", abra o arquivo de mesmo nome lá e preserve o comportamento e os testes.

## Global Constraints

- JDK: `D:\Workspace\Languages-Frameworks\jdk-25.0.4.1+1`; `<java.version>25</java.version>`; sem `--enable-preview`.
- Spring Boot ≥ 4.0. Use o mais novo que o release train do Spring Cloud suportar. Confirme as versões no context7 / Maven Central, nunca de memória.
- Pacote base `com.your.summoner`; context path `/your-summoner`; porta 8080.
- `domain`: sem Spring, sem Lombok, sem MapStruct, sem Jackson. `application`: só `@Service`/`@Component` do Spring. `infrastructure`: o resto. Travado pelo `ArchitectureTest`.
- Chave da Riot só por `riot.api.token=${RIOT_API_KEY:}`. Nunca use o nome `riot.api.key` (ADR 0001 g). Os testes usam `test-key` e nunca podem enviar a chave real.
- Mongo: `${MONGODB_URI:mongodb://127.0.0.1:27017/yoursummoner}`. Confirme no context7 se o Boot 4 usa `spring.mongodb.uri` ou `spring.data.mongodb.uri`.
- Locale: `yoursummoner.locale=pt_BR`. Sem `?locale=` nesta entrega.
- Job de patch: `yoursummoner.patch-sync.cron=0 0 * * * *`, `yoursummoner.patch-sync.enabled=true` (`false` no perfil de teste). Trava de ingestão abandonada após 15 min.
- Erros sempre `application/problem+json` (RFC 9457) via `ProblemDetail`, sem stack trace.
- CORS: `http://localhost:4200`, `http://localhost:3000`; métodos GET, OPTIONS, HEAD.
- Nada de push nem PR. Commit local ao fim de cada tarefa, com o trailer `Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>`.
- Comandos Maven com `JAVA_HOME` apontando para o JDK 25 acima.

## Review Focus

1. **Requisição durante a troca de patch:** quem consulta enquanto o patch novo está `INGESTING` recebe o patch anterior, nunca dados parciais. Teste na Task 7: `activePatchStaysOldWhileNewIsIngesting`.
2. **`?patch=` inválido ou inexistente** (`abc`, `99.99.1`): 404 RFC 9457, sem chamar a ingestão. Teste na Task 7 (`unknownPatchIsNotFoundWithoutIngestion`) e na Task 8 (`unknownPatchReturns404Problem`).
3. **Nome de campeão em caixa diferente ou com espaço/apóstrofo** (`ahri`, `Kai'Sa`, `Miss Fortune`): mesma resolução do Quarkus. Porte o teste do `ChampionServiceTest` e adicione `resolvesNameCaseInsensitively` na Task 8.
4. **Chave real no `.env` vazando para os testes:** os testes do WireMock verificam o header `X-Riot-Token: test-key`. Teste na Task 3: `requestsCarryTestKeyOnly`.
5. **Mongo fora do ar durante uma consulta:** leitura direta do Data Dragon com WARN, sem 500. Teste na Task 7: `mongoDownFallsBackToDataDragon`.

---

### Task 1: Bootstrap do projeto Spring Boot 4

**Delegado:** Codex `gpt-5.6-terra`.

**Files:**
- Delete: tudo em `src/` do código de 2023 (`git rm -r src`), `HELP.md` se houver.
- Modify: `pom.xml` (reescrever), `.gitignore` (garantir `.env`, `target/`).
- Create: `src/main/java/com/your/summoner/YourSummonerApplication.java`, `src/main/resources/application.yml`, `src/test/resources/application-test.yml`, `src/test/java/com/your/summoner/ApplicationSmokeTest.java`, `src/test/java/com/your/summoner/ArchitectureTest.java`.
- Copy da referência: `.claude/launch.json`, `.claude/riot-mock.py`, `docs/adr/0001-decisoes-da-migracao-quarkus.md` (como histórico, sem editar).
- Modify: `AGENTS.md`, `CLAUDE.md` (stack Spring + tabela de skills do superpowers, copiada de `D:\Workspace\mandato-a-vista\AGENTS.md` linhas 24-34).

**Interfaces:**
- Produces: build Maven funcional; perfil `test`; `@EnableFeignClients`, `@EnableCaching`, `@EnableScheduling`, `@ConfigurationPropertiesScan` na classe principal.

- [ ] **Step 1:** `pom.xml` com `spring-boot-starter-parent` 4.x, BOM `spring-cloud-dependencies`, starters `web`, `validation`, `cache`, `data-mongodb`, `actuator`; `spring-cloud-starter-openfeign`, `spring-cloud-starter-circuitbreaker-resilience4j`; `caffeine`; `springdoc-openapi-starter-webmvc-ui`; `mapstruct` + `mapstruct-processor` + `lombok` + `lombok-mapstruct-binding` no `annotationProcessorPaths`; teste: `spring-boot-starter-test`, `wiremock-standalone` (ou `wiremock-spring-boot`), `archunit-junit5`, `de.flapdoodle.embed.mongo.spring3x` (versão compatível com Boot 4). Plugin `jacoco-maven-plugin` com `prepare-agent` e `report` no `verify`.
- [ ] **Step 2:** `ArchitectureTest` com as mesmas regras do Quarkus, trocando "CDI" por "Spring": `domain` não depende de `org.springframework..`, `lombok..`, `org.mapstruct..`, `com.fasterxml..`, nem de `application`/`infrastructure`; `application` não depende de `infrastructure` e só usa `org.springframework.stereotype..` do Spring.
- [ ] **Step 3:** `ApplicationSmokeTest` (`@SpringBootTest`, perfil `test`, Mongo embarcado) verifica que o contexto sobe e `GET /your-summoner/actuator/health` responde 200.
- [ ] **Step 4:** Rodar `mvn -q verify`. Esperado: BUILD SUCCESS, relatório em `target/site/jacoco/index.html`.
- [ ] **Step 5:** Commit `build: inicia projeto Spring Boot 4 hexagonal em Java 25`.

### Task 2: Domínio

**Delegado:** Ollama `qwen3:14b` gera os records novos; Codex `gpt-5.6-luna` copia o domínio e os testes.

**Files:**
- Copy sem mudança: `domain/**` e os testes `domain/model/DomainModelTest.java`, `domain/service/MasteryRankingTest.java` da referência.
- Create: `domain/model/Patch.java`, `domain/model/PatchStatus.java`, `domain/model/ChampionSkin.java`.
- Test: `src/test/java/com/your/summoner/domain/model/PatchTest.java`.

**Interfaces:**
- Produces:
  - `enum PatchStatus { NOT_INGESTED, INGESTING, READY, FAILED }`
  - `record Patch(String version, boolean active, Map<String, PatchStatus> statusByLocale)`: construtor compacto copia o mapa (`Map.copyOf`, `null` vira vazio); `PatchStatus statusFor(String locale)` devolve `NOT_INGESTED` se ausente; `boolean isReady(String locale)`.
  - `record ChampionSkin(String championId, String skinId, int num, String name, String splashUrl, String loadingUrl)`.
  - `Patch.isValidVersion(String v)`: `true` só para `\d+\.\d+\.\d+`.

- [ ] **Step 1:** Teste `PatchTest`: `statusForUnknownLocaleIsNotIngested`, `isReadyOnlyWhenLocaleReady`, `mapIsImmutable`, `isValidVersion("14.1.1") == true`, `isValidVersion("abc") == false`, `isValidVersion("lolpatch_3.7") == false`.
- [ ] **Step 2:** Rodar `mvn -q test -Dtest=PatchTest`. Esperado: FAIL (classe ausente).
- [ ] **Step 3:** Implementar os três tipos.
- [ ] **Step 4:** Rodar `mvn -q test -Dtest='PatchTest,DomainModelTest,MasteryRankingTest'`. Esperado: PASS.
- [ ] **Step 5:** Commit `feat: porta o domínio e adiciona Patch e ChampionSkin`.

### Task 3: Adapters da Riot (OpenFeign + Resilience4j + Caffeine)

**Delegado:** Codex `gpt-5.6-sol` (spec passo a passo abaixo).

**Files:**
- Create em `infrastructure/adapter/out/riot/`: interfaces Feign `RiotAccountApi`, `RiotSummonerApi`, `RiotLeagueApi`, `RiotChampionMasteryApi`, `RiotMatchApi`; `RiotFeignConfig` (interceptor do header, `ErrorDecoder`, timeouts); `RiotErrorDecoder`; os 5 adapters; `RiotMapper` (MapStruct); DTOs em `dto/` (portar como records).
- Create: `infrastructure/config/RiotApiProperties.java` (`@ConfigurationProperties("riot.api")`: `token`, `baseUrlTemplate`, `connectTimeout=2s`, `readTimeout=4s`).
- Create: `application/port/out/*Provider.java` da Riot (copiar da referência sem mudança).
- Test: portar `RiotAdapterErrorHandlingTest`, `RiotAdapterResilienceTest`, `RiotAdapterCacheTest` e o `support/RiotApiWireMock`.

**Interfaces:**
- Consumes: domínio (Task 2).
- Produces: beans de `AccountProvider`, `SummonerProvider`, `LeagueProvider`, `ChampionMasteryProvider`, `MatchProvider` com as assinaturas da referência.
- Produces: `application/port/out/RiotCredentials.java` = `public interface RiotCredentials { Optional<String> riotApiKey(); }`, lido pelo interceptor do Feign **a cada requisição** (a chave pode mudar a quente pela Task 12). Bean de fallback `infrastructure/config/EnvRiotCredentials` (`@ConditionalOnMissingBean(RiotCredentials.class)`), que devolve `riot.api.token` se não vazio.
- Já existem no repositório (não recrie): `infrastructure/adapter/out/{ExternalCalls,CachedCalls,RetryableFailure,UpstreamNotFoundException}.java`.

- [ ] **Step 1:** Portar os 3 testes. Adicionar `requestsCarryTestKeyOnly`: todo request recebido pelo WireMock tem `X-Riot-Token: test-key`.
- [ ] **Step 2:** Rodar. Esperado: FAIL.
- [ ] **Step 3:** Host dinâmico: cada método Feign recebe `URI baseUri` como primeiro parâmetro, montado de `baseUrlTemplate` com `{host}` = plataforma ou região em minúsculas. Não crie um client por host.
- [ ] **Step 4:** `RiotErrorDecoder`: 401/403 → `ExternalAuthenticationException`; 404 → `UpstreamNotFoundException` (adapter devolve `Optional.empty()`); 429 → `RateLimitExceededException` com o valor de `Retry-After`; 5xx → `ExternalServiceUnavailableException`; corpo ilegível → `ExternalBadResponseException`. Token vazio → `ExternalAuthenticationException` sem chamar a rede.
- [ ] **Step 5:** Resilience4j por adapter: retry 3 tentativas só em `ExternalServiceUnavailableException` e timeout; circuit breaker contando só essas duas (ADR 0001 b). Um circuit breaker por adapter, não um global.
- [ ] **Step 6:** Caffeine com os nomes e TTLs da referência (`riot-account` 30m, `riot-summoner` 5m, `riot-league` 2m, `riot-mastery` 10m, `riot-match-ids` 1m, `riot-match` 24h). Chave do account em minúsculas. "Não encontrado" não entra no cache (porte o padrão `CachedCalls`).
- [ ] **Step 7:** Rodar os testes. Esperado: PASS.
- [ ] **Step 8:** Commit `feat: adapters da Riot com OpenFeign, Resilience4j e Caffeine`.

### Task 4: Serviços da Riot na application

**Delegado:** Codex `gpt-5.6-sol`.

**Files:**
- Create: `application/port/in/{GetMatchUseCase,GetMatchHistoryUseCase,ListMatchIdsUseCase,GetSummonerProfileUseCase}.java` (copiar), `application/service/{AccountResolver,MatchService}.java` (portar com `@Service`).
- Test: portar `MatchServiceTest`.

**Interfaces:**
- Consumes: providers da Task 3.
- Produces: `MatchService implements GetMatchUseCase, GetMatchHistoryUseCase, ListMatchIdsUseCase`; `AccountResolver.resolve(RiotId, Platform): Account`.

- [ ] **Step 1:** Portar `MatchServiceTest`. Adicionar `historyRunsAtMostFourCallsAtOnce`, com um fake de `MatchProvider` que mede o pico de chamadas simultâneas: pico ≤ 4.
- [ ] **Step 2:** Rodar. Esperado: FAIL.
- [ ] **Step 3:** Fan-out do histórico com `Executors.newVirtualThreadPerTaskExecutor()` + `Semaphore(4)`, igual ao Quarkus (ADR 0001 f). 404 de uma partida é pulado; 429/503 derruba a requisição.
- [ ] **Step 4:** Rodar. Esperado: PASS.
- [ ] **Step 5:** Commit `feat: porta os serviços de partida para Spring`.

### Task 5: Adapters do Data Dragon e do Community Dragon

**Delegado:** Codex `gpt-5.6-sol`.

**Files:**
- Create: `application/port/out/GameDataProvider.java` (**assinatura nova**, abaixo), `application/port/out/ChampionStatisticsProvider.java` (copiar).
- Create: `infrastructure/adapter/out/ddragon/{DataDragonApi,DataDragonAdapter}.java` + DTOs; `infrastructure/adapter/out/cdragon/{CommunityDragonApi,CommunityDragonAdapter}.java`.
- Test: portar `DataDragonAdapterTest`, `CommunityDragonAdapterTest`.

**Interfaces:**
- Produces:
  ```java
  public interface GameDataProvider {
      List<String> versions();
      Map<Long, Champion> champions(String patch, String locale);
      Optional<ChampionDetail> championDetail(String patch, String locale, String championId);
      String profileIconUrl(String patch, int profileIconId);
      String championIconUrl(String patch, String championId);
      String skinSplashUrl(String championId, int num);
      String skinLoadingUrl(String championId, int num);
  }
  ```
  O adapter não guarda estado: o "último catálogo bom" do Quarkus passa a ser o Mongo (Task 7).

- [ ] **Step 1:** Portar os testes, adaptando para a assinatura nova. Manter: chromas removidas das skins; 403/404 em `versions()`/`champions()` viram `ExternalServiceUnavailableException`; `championDetail` 404 → `Optional.empty()`. Adicionar `skinUrlsFollowDataDragonPattern`: splash = `https://ddragon.leagueoflegends.com/cdn/img/champion/splash/Ahri_1.jpg`, loading = `.../cdn/img/champion/loading/Ahri_1.jpg`.
- [ ] **Step 2:** Rodar. Esperado: FAIL.
- [ ] **Step 3:** Implementar. Timeouts de 2s de conexão e 8s de leitura. Retry só em falha transitória.
- [ ] **Step 4:** Rodar. Esperado: PASS.
- [ ] **Step 5:** Commit `feat: adapters do Data Dragon e do Community Dragon`.

### Task 6: Repositório do catálogo no MongoDB

**Delegado:** Codex `gpt-5.6-sol`.

**Files:**
- Create: `application/port/out/StaticDataRepository.java`.
- Create em `infrastructure/adapter/out/mongo/`: `PatchDocument`, `ChampionDocument`, `ChampionDetailDocument`, `SkinDocument`, `ChampionStatisticsDocument`, `CatalogDocumentMapper` (MapStruct), `MongoStaticDataRepository`, `MongoIndexes` (cria os índices únicos na subida).
- Test: `MongoStaticDataRepositoryTest` (Mongo embarcado).

**Interfaces:**
- Produces:
  ```java
  public interface StaticDataRepository {
      List<Patch> findAllPatches();                       // ordem de versions.json (mais nova primeiro)
      Optional<Patch> findPatch(String version);
      Optional<Patch> findActivePatch();
      void mergeVersions(List<String> versions);          // insere as novas como NOT_INGESTED; não mexe nas existentes
      boolean tryLock(String version, String locale, Instant now, Duration staleAfter);
      void markReady(String version, String locale);
      void markFailed(String version, String locale);
      void activate(String version);                      // desativa as demais na mesma operação lógica
      void saveChampions(String patch, String locale, Collection<Champion> champions);
      void saveChampionDetail(String patch, String locale, ChampionDetail detail);
      void saveSkins(String patch, String locale, List<ChampionSkin> skins);
      void savePickRates(String patch, Lane lane, Map<Long, Double> pickRates);
      Map<Long, Champion> findChampions(String patch, String locale);   // vazio se não houver
      Optional<ChampionDetail> findChampionDetail(String patch, String locale, String championId);
      List<ChampionSkin> findSkins(String patch, String locale, String championId); // ordenado por num
      Optional<Map<Long, Double>> findPickRates(String patch, Lane lane);
  }
  ```
  Índices únicos: `patches.version`; `champions {patch, locale, championId}`; `champion_details {patch, locale, championId}`; `skins {patch, locale, championId, num}`; `champion_statistics {patch, lane}`. Saves são upsert (idempotentes).

- [ ] **Step 1:** Testes: `mergeVersionsKeepsExistingStatus`, `tryLockIsExclusive` (segundo `tryLock` falha), `tryLockTakesOverStaleLock` (lock com 16 min é retomado), `activateLeavesSingleActive`, `saveIsIdempotent` (salvar 2x não duplica), `skinsOrderedByNum`, `localesAreIsolated` (`pt_BR` e `en_US` do mesmo patch não se misturam).
- [ ] **Step 2:** Rodar. Esperado: FAIL.
- [ ] **Step 3:** Implementar. `tryLock` atômico com `findAndModify`: condição status do locale ≠ `INGESTING`, ou `lockedAt < now - staleAfter`.
- [ ] **Step 4:** Rodar. Esperado: PASS.
- [ ] **Step 5:** Commit `feat: repositório do catálogo estático no MongoDB`.

### Task 7: Ingestão de patch e leitura do catálogo

**Delegado:** Codex `gpt-5.6-sol`, com revisão crítica do Opus (lógica de concorrência).

**Files:**
- Create: `application/port/in/SyncPatchesUseCase.java`, `application/port/in/ListPatchesUseCase.java`.
- Create: `application/service/CatalogSettings.java`, `application/service/PatchService.java`, `application/service/ChampionCatalog.java`.
- Create: `infrastructure/config/CatalogConfig.java` (bean `CatalogSettings` a partir de `yoursummoner.*`), `infrastructure/adapter/in/scheduler/PatchSyncScheduler.java`, `infrastructure/config/CatalogStartup.java` (substitui o `StaticDataWarmup`).
- Test: `PatchServiceTest`, `ChampionCatalogTest` (Mockito, fakes dos ports), `PatchSyncSchedulerTest`.

**Interfaces:**
- Consumes: `GameDataProvider` (Task 5), `ChampionStatisticsProvider` (Task 5), `StaticDataRepository` (Task 6).
- Produces:
  - `record CatalogSettings(String locale, Duration lockStaleAfter)`.
  - `interface SyncPatchesUseCase { void syncLatest(); void ingest(String version); }`.
  - `interface ListPatchesUseCase { List<Patch> execute(); }`.
  - `PatchService implements SyncPatchesUseCase, ListPatchesUseCase`.
  - `ChampionCatalog` (`@Service`, usado pelos serviços da Task 8):
    - `String activePatch()`; `String resolve(String requestedPatch)`: `null` ou vazio → ativo; versão inválida ou desconhecida → `ResourceNotFoundException`.
    - `Map<Long, Champion> champions(String patch)`; `Optional<ChampionDetail> championDetail(String patch, String championId)`; `List<ChampionSkin> skins(String patch, String championId)`; `Map<Long, Double> pickRates(Lane lane)`.
    - `String profileIconUrl(String patch, int id)`; `String championIconUrl(String patch, String championId)`.

- [ ] **Step 1:** Testes do `PatchService`:
  - `syncLatestIngestsThenActivates`: versão nova → `mergeVersions`, `tryLock`, saves, `markReady` e só depois `activate` (verifique a ordem com `InOrder`).
  - `activePatchStaysOldWhileNewIsIngesting`: durante a ingestão, `activePatch()` devolve o anterior.
  - `failedIngestionKeepsPreviousActive`: exceção no meio → `markFailed`, `activate` nunca chamado.
  - `syncLatestIsNoOpWhenLatestAlreadyReady`.
  - `lockedPatchIsSkipped`: `tryLock` falso → nenhum save.
  - `localeSwitchIngestsActivePatchInNewLocale`: com `CatalogSettings("en_US", 15m)` e o patch ativo `READY` só em `pt_BR`, `syncLatest()` ingere o patch ativo em `en_US` sem trocar o ativo.
  - Ingestão grava `champions`, um `champion_details` e as `skins` por campeão (com as URLs do `GameDataProvider`), e o pick rate de cada `Lane`. Os detalhes são buscados em virtual threads com no máximo 8 simultâneos.
- [ ] **Step 2:** Testes do `ChampionCatalog`:
  - `readsFromMongoWhenReady` (não chama `GameDataProvider`).
  - `oldPatchIsIngestedOnDemand`: patch conhecido mas `NOT_INGESTED` → `ingest(version)` e depois lê do Mongo.
  - `unknownPatchIsNotFoundWithoutIngestion`: `"99.99.1"` e `"abc"` → `ResourceNotFoundException`; `ingest` nunca chamado.
  - `mongoDownFallsBackToDataDragon`: repositório lança `DataAccessException` → resultado do `GameDataProvider`, sem exceção.
  - `noCatalogAtAllIsServiceUnavailable`: Mongo vazio + Data Dragon fora → `ExternalServiceUnavailableException`.
- [ ] **Step 3:** Rodar. Esperado: FAIL.
- [ ] **Step 4:** Implementar. Caffeine L1 nos métodos de leitura do `MongoStaticDataRepository` (Task 6): caches `catalog-champions`, `catalog-detail`, `catalog-skins` sem TTL e com `maximumSize` (dados imutáveis); `catalog-active-patch` com 1 min.
- [ ] **Step 5:** `PatchSyncScheduler`: `@Scheduled(cron = "${yoursummoner.patch-sync.cron}")` chama `syncLatest()`, desligado com `yoursummoner.patch-sync.enabled=false`. `CatalogStartup`: na subida, em virtual thread, chama `syncLatest()`; falha só gera WARN.
- [ ] **Step 6:** Rodar. Esperado: PASS.
- [ ] **Step 7:** Commit `feat: ingestão automática de patch e catálogo em duas camadas`.

### Task 8: Serviços de campeão e camada REST

**Delegado:** Codex `gpt-5.6-sol`; Ollama gera os DTOs de resposta e os mappers MapStruct triviais.

**Files:**
- Create: `application/port/in/{GetChampionDetailUseCase,GetGameVersionUseCase,ListChampionMasteriesUseCase,ListLaneChampionsUseCase,ListChampionSkinsUseCase}.java` (assinaturas abaixo), `application/service/{ChampionService,SummonerProfileService}.java` (portar, trocando `GameDataProvider` por `ChampionCatalog`).
- Create em `infrastructure/adapter/in/rest/`: `ChampionController`, `MatchController`, `SummonerController`, `PatchController`, `PlatformResolver`, `ProblemDetailsHandler` (`@RestControllerAdvice`), DTOs em `dto/` (portar + `ChampionSkinResponse`, `PatchResponse`), `RestDtoMapper` (MapStruct).
- Create: `infrastructure/config/OpenApiConfig.java`, `infrastructure/config/WebConfig.java` (CORS).
- Test: portar `ChampionServiceTest`, `SummonerProfileServiceTest`, `ChampionResourceTest`, `MatchResourceTest`, `SummonerResourceTest` como `*ControllerTest` (MockMvc), e criar `PatchControllerTest`.

**Interfaces:**
- Consumes: `ChampionCatalog`, `ListPatchesUseCase` (Task 7); use cases da Task 4.
- Produces:
  - `ChampionDetail GetChampionDetailUseCase.execute(String championName, String patch)`
  - `String GetGameVersionUseCase.execute()` → patch ativo
  - `List<MasteredChampion> ListChampionMasteriesUseCase.execute(RiotId riotId, Platform platform, String patch)`
  - `List<MasteredChampion> ListLaneChampionsUseCase.execute(RiotId riotId, Platform platform, Lane lane, String patch)`
  - `List<ChampionSkin> ListChampionSkinsUseCase.execute(String championName, String patch)`
  - Endpoints: os 8 da seção 5 do spec, com os mesmos paths e JSON da referência; `?patch=` opcional em `find-champion`, `by-mastery` e lane; `GET /patches`; `GET /champions/{championName}/skins?patch=`.

- [ ] **Step 1:** Portar os testes. Adicionar: `resolvesNameCaseInsensitively` (`ahri`, `Kai'Sa`, `Miss Fortune`), `unknownPatchReturns404Problem` (`content-type: application/problem+json`, `status: 404`, sem `trace`), `skinsEndpointReturnsSplashAndLoading`, `patchesEndpointMarksActive`, `rateLimitReturns429WithRetryAfter`, `invalidLaneReturns400Problem` (Jakarta Validation).
- [ ] **Step 2:** Rodar. Esperado: FAIL.
- [ ] **Step 3:** Implementar. Os contratos JSON têm que bater campo a campo com `docs/openapi/openapi.json` da referência; os campos novos só podem ser adições.
- [ ] **Step 4:** springdoc em `/your-summoner/swagger-ui.html`. Exportar a especificação para `docs/openapi/openapi.json` e `openapi.yaml` (teste ou plugin `springdoc-openapi-maven-plugin`).
- [ ] **Step 5:** Rodar `mvn -q verify`. Esperado: PASS.
- [ ] **Step 6:** Commit `feat: serviços de campeão, endpoints REST, skins e patches`.

### Task 9: Base de conhecimento LoL, roadmap e ADR 0002

**Delegado:** Gemini via FreeLLMAPI faz o rascunho; o Opus revisa os fatos.

**Files:**
- Create: `docs/lol/README.md` (índice), `docs/lol/patches-e-versoes.md`, `docs/lol/data-dragon-e-community-dragon.md`, `docs/lol/riot-api-roteamento-e-limites.md`, `docs/lol/modos-de-jogo.md`, `docs/roadmap.md`, `docs/adr/0002-port-para-spring-boot-4.md`.
- Modify: `README.md`, `CLAUDE.md` e `AGENTS.md` (link para `docs/lol/` e `docs/roadmap.md`).

- [ ] **Step 1:** `docs/lol/` com o conteúdo da seção 7 do spec: cadência de quarta-feira a cada 2 semanas com exceção para hotfix; atraso do Data Dragon; hotfix sem versão nova; versão do jogo vs. versão do Data Dragon; URLs e arquivos usados; plataforma vs. região; rate limit e dev key de 24h; Arena (`CHERRY`, subteams, colocação) e filas.
- [ ] **Step 2:** `docs/roadmap.md` em fases: F1 = este plano; F2 = idioma por requisição (`?locale=`/`Accept-Language`); F3 = ingestão em massa de patches antigos e diff entre patches; F4 = itens, runas e feitiços; F5 = rate limiter por host e OpenTelemetry.
- [ ] **Step 3:** ADR 0002 com as decisões do spec (seção 10) e o aviso de que o OpenFeign está em manutenção.
- [ ] **Step 4:** Commit `docs: base de conhecimento LoL, roadmap e ADR do port`.

### Task 10: Instalação do MongoDB (independente, pode rodar em paralelo desde o início)

**Delegado:** agente `ops-runner` (Haiku).

**Files:** nenhum no repositório, exceto `.claude/launch.json` (entrada `mongodb`).

- [ ] **Step 1:** Baixar MongoDB Community Server 8.x (zip Windows x64) e `mongosh` de `fastdl.mongodb.org` / `downloads.mongodb.com` para `D:\Workspace\Languages-Frameworks\_downloads`, conferindo o SHA-256 publicado; registrar em `checksums.txt`.
- [ ] **Step 2:** Extrair em `D:\Workspace\Languages-Frameworks\mongodb-<versão>` e `mongosh-<versão>`. Dados em `D:\Workspace\Languages-Frameworks\mongodata-yoursummoner`.
- [ ] **Step 3:** Subir com `mongod --dbpath ... --bind_ip 127.0.0.1 --port 27017`. Sem serviço do Windows e sem PATH global.
- [ ] **Step 4:** Verificar: `mongosh --eval "db.runCommand({ping:1})"` → `{ ok: 1 }`.

### Task 12: Administração de segredos e parâmetros (API)

**Delegado:** Codex `gpt-5.6-sol`; o Opus revisa toda a parte de segurança.

**Files:**
- Create em `domain/model/`: `SecretName` (`enum { RIOT_API_KEY }`), `SecretMetadata(SecretName name, String last4, Instant updatedAt, String updatedBy)` (sem o valor; `toString` padrão do record é seguro), `AppSettings(String locale, Platform defaultPlatform)`.
- Create em `application/port/out/`: `SecretStore`, `SettingsStore`, `AuditLog`. Em `application/port/in/`: `ManageSecretsUseCase`, `ManageSettingsUseCase`. Em `application/service/`: `CredentialService implements RiotCredentials`, `AdminService implements ManageSecretsUseCase, ManageSettingsUseCase`.
- Create em `infrastructure/adapter/out/mongo/`: `SecretDocument`, `SettingsDocument`, `AuditDocument`, `MongoSecretStore`, `MongoSettingsStore`, `MongoAuditLog`; em `infrastructure/security/`: `AesGcmCipher`, `MasterKey` (lê `YOURSUMMONER_MASTER_KEY`), `SecurityConfig`, `AdminAuthService` (BCrypt + TOTP + JWT), `LoginRateLimiter`; em `infrastructure/adapter/in/rest/admin/`: `AdminSessionController`, `AdminSecretsController`, `AdminSettingsController`, `AdminAuditController` + DTOs.
- Modify: `pom.xml` (`spring-boot-starter-security`, `spring-boot-starter-oauth2-resource-server` para validar o JWT, uma lib TOTP pequena como `dev.samstevens.totp:totp`), `application.yml` (`yoursummoner.admin.allowed-cidrs: 127.0.0.1/32,::1/128`, `yoursummoner.admin.user: ${YOURSUMMONER_ADMIN_USER:}`, `yoursummoner.admin.password-hash: ${YOURSUMMONER_ADMIN_PASSWORD_HASH:}`, `yoursummoner.master-key: ${YOURSUMMONER_MASTER_KEY:}`).
- Modify: `ChampionCatalog`/`CatalogSettings` (Task 7) e `PlatformResolver` (Task 8) para ler `locale` e `defaultPlatform` do `SettingsStore` a cada uso, com o `application.yml` como valor inicial.
- Test: `AesGcmCipherTest`, `CredentialServiceTest`, `AdminServiceTest`, `AdminSecurityTest` (MockMvc), `SecretLeakTest`.

**Interfaces:**
- `SecretStore { Optional<String> find(SecretName name); void put(SecretName name, String value, String actor); List<SecretMetadata> list(); }`
- `SettingsStore { AppSettings get(); void put(AppSettings settings, String actor); }`
- `AuditLog { void record(String actor, String action, String target, String ip); List<AuditEntry> recent(int limit); }` com `record AuditEntry(String actor, String action, String target, String ip, Instant at)` em `domain/model`.
- REST: `POST /admin/session` `{user, password, totp}` → `{token, expiresAt}` ou, no primeiro login, `{token, expiresAt, totpEnrollmentUri}`; `GET /admin/secrets` → lista de `SecretMetadata`; `PUT /admin/secrets/{name}` `{value}` → 204; `GET/PUT /admin/settings`; `GET /admin/audit?limit=50`.

- [ ] **Step 1:** Testes:
  - `AesGcmCipherTest`: `roundTrip`, `sameValueGivesDifferentCiphertext` (IV aleatório), `tamperedCiphertextFails`, `wrongKeyFails`.
  - `CredentialServiceTest`: `vaultValueWinsOverEnv`, `fallsBackToEnvWhenVaultEmpty`, `putIsVisibleOnNextCall` (sem reiniciar).
  - `AdminSecurityTest`: `publicRoutesStayOpen` (`/champions/findVersion` sem token → 200); `adminWithoutTokenIs401`; `loginWithWrongPasswordIs401Generic` (mesma mensagem para usuário ou senha errados); `sixthFailedLoginIn15MinIs429`; `firstLoginReturnsEnrollmentUriOnce`; `loginWithoutTotpAfterEnrollmentIs401`; `expiredTokenIs401`; `remoteIpIsForbidden` (requisição de `10.0.0.5` → 403); `adminDisabledWithoutMasterKey` (`/admin/**` → 503 problem).
  - `SecretLeakTest`: após `PUT /admin/secrets/RIOT_API_KEY` com `RGAPI-segredo-de-teste-1234`, o valor não aparece em nenhuma resposta, no log capturado (`OutputCaptureExtension`), no documento Mongo (só ciphertext) nem no `admin_audit`; `GET /admin/secrets` devolve `last4 = "1234"`.
- [ ] **Step 2:** Rodar. Esperado: FAIL.
- [ ] **Step 3:** Implementar. JWT HS256 com chave derivada da master key por HKDF-SHA256 (`info = "yoursummoner-admin-jwt"`), expiração de 15 min. CSRF desligado só em `/admin/**` (API stateless com Bearer); público continua só GET/OPTIONS/HEAD.
- [ ] **Step 4:** Rodar `mvn -q verify`. Esperado: PASS.
- [ ] **Step 5:** Documentar em `docs/admin.md`: como gerar a master key (`[Convert]::ToBase64String((1..32 | % {Get-Random -Max 256}) -as [byte[]])` só como exemplo; recomendar `RandomNumberGenerator`), como gerar o hash BCrypt e que essas 3 variáveis vão no `.env`, nunca no git.
- [ ] **Step 6:** Commit `feat: administração de segredos e parâmetros com cofre criptografado`.

### Task 13: Tela de administração no YourSummoner-Web

**Delegado:** subagente `go-web-developer` (repositório `D:\Workspace\YourSummoner-Web`).

**Files:** em `YourSummoner-Web`: handlers e templates de `/admin` (login, segredos, parâmetros, auditoria), client tipado para `/admin/**`, testes com `httptest`.

- [ ] **Step 1:** Testes `httptest`: login ok grava cookie `HttpOnly; SameSite=Strict; Path=/admin`; formulário sem token CSRF → 403; a página de segredos mostra só `••••1234`; campo de segredo é `type=password` e nunca vem preenchido; 401 da API redireciona para o login; mostra o QR/URI de TOTP só na primeira vez.
- [ ] **Step 2:** Implementar só com stdlib (`net/http`, `html/template`), seguindo o padrão já existente no repositório.
- [ ] **Step 3:** `go test ./...` e `go vet ./...` passam.
- [ ] **Step 4:** Commit local no `YourSummoner-Web`.

### Task 11: Verificação final

**Delegado:** `quarkus-auditor` adaptado (somente leitura) + Opus.

- [ ] **Step 1:** `mvn verify`. Esperado: todos os testes passam; anotar a cobertura do JaCoCo.
- [ ] **Step 2:** Subir a app contra o Mongo local da Task 10 com `RIOT_API_KEY` do `.env`. Conferir que `GET /your-summoner/patches` lista versões e um patch fica `READY`.
- [ ] **Step 3:** Rodar com `yoursummoner.locale=en_US` e conferir que `find-champion/Ahri` vem em inglês e o `pt_BR` continua no Mongo.
- [ ] **Step 4:** Trocar a chave da Riot pela tela `/admin` do Web e conferir que a chamada seguinte usa a chave nova sem reiniciar.
- [ ] **Step 5:** Auditoria somente leitura contra o spec e o ADR 0001, com relatório HIGH/MEDIUM/LOW, incluindo revisão de segurança da Task 12.
