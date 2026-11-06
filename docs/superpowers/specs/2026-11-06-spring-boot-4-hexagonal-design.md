# Design — YourSummoner-API em Spring Boot 4, hexagonal, com catálogo do Data Dragon no MongoDB

- Data: 2026-11-06
- Branch de trabalho: `feature/spring-boot-4-hexagonal` (criada a partir de `develop`, que está em `origin/develop`)
- Referência: branch local `feature/quarkus-hexagonal` (commit `5bdfb37`), aberta como worktree em `D:\Workspace\YourSummoner-API-quarkus`. Essa branch **não** vai para o remoto.

## 1. Intenção

A API já foi reescrita em Quarkus 3.40.1 / Java 25 com arquitetura hexagonal. O usuário quer voltar ao Spring Boot, agora na versão 4, **sem perder nada** do que foi feito no Quarkus: endpoints, regras de negócio, tratamento de erro, resiliência, cache, suporte ao modo Arena, testes e ADRs.

Além do port, entram duas coisas novas:

1. **Catálogo do Data Dragon persistido no MongoDB, versionado por patch.** Os dicionários do Data Dragon são grandes e imutáveis por patch, então não faz sentido baixá-los a cada subida ou a cada consulta.
2. **Troca automática de patch.** Quando a Riot publica um patch novo, a API passa a usá-lo sozinha. Patches antigos continuam consultáveis.

O projeto também ganha uma base de conhecimento sobre League of Legends e um roadmap. O usuário vai voltar a esses assuntos com frequência e quer a informação rápida e acessível.

## 2. Objetivos

- Paridade funcional com a versão Quarkus: mesmos endpoints, mesmo context path `/your-summoner`, mesmos contratos JSON, mesmas regras de erro (ver seção 5).
- Stack definida pelo usuário (seção 4).
- MongoDB como cache persistente (L2) do catálogo estático, com Caffeine em memória na frente (L1).
- Lista completa de versões (`versions.json`) persistida.
- Ingestão do patch ativo inteira. Ingestão de patch antigo sob demanda, na primeira consulta.
- Base de conhecimento LoL em `docs/lol/` e roadmap em `docs/roadmap.md`.
- Fluxo de trabalho com superpowers documentado em `AGENTS.md` e `CLAUDE.md`, igual ao do projeto Mandato à Vista.
- MongoDB instalado na máquina seguindo a regra do workspace.

## 3. Não-objetivos e anti-objetivos

- **Não** guardar nada do jogador: partidas, histórico, performance, rank ou perfil. Isso continua só em cache de memória com TTL curto. Decisão explícita do usuário.
- **Não** fazer agora: ingestão em massa de todos os patches antigos, nem diff entre patches. Vão para o roadmap.
- **Não** dar push de nenhuma branch, nem abrir PR, sem pedido explícito.
- **Não** apagar a versão Quarkus.
- Anti-objetivo: perder comportamento que o Quarkus já tinha (Arena/subteam, filtro de chromas nas skins, 429 com `Retry-After`, fallback do último catálogo bom). Se a versão Spring funcionar mas perder um desses, a entrega falhou.
- Anti-objetivo: uma requisição de usuário cair num patch pela metade.

## 4. Stack (decidida pelo usuário)

| Tema | Escolha |
|---|---|
| Linguagem | Java 25 (JDK em `D:\Workspace\Languages-Frameworks\jdk-25.0.4.1+1`) |
| Framework | Spring Boot 4.x (a mais recente estável no Maven Central na hora de implementar, nunca abaixo de 4.0) |
| Validação | Jakarta Validation (`spring-boot-starter-validation`) |
| APIs externas | Spring Cloud OpenFeign (release train do Spring Cloud compatível com a versão do Boot) |
| Documentação | OpenAPI com springdoc-openapi (Swagger UI), exportando a especificação para `docs/openapi/` |
| Testes | JUnit 5, Mockito, WireMock, JaCoCo |
| Conversões | MapStruct para DTO ↔ domínio, Lombok |
| Persistência | MongoDB com Spring Data MongoDB |
| Java 25 | records, text blocks/strings, virtual threads e concorrência estruturada para fan-out |

Aviso para o builder: o Spring Cloud OpenFeign está em modo de manutenção, e o Spring recomenda HTTP Interface clients. O usuário escolheu o OpenFeign mesmo assim, então use o OpenFeign. Registre o aviso no ADR.

## 5. O que precisa ser portado (inventário do Quarkus)

Fonte de verdade: worktree `D:\Workspace\YourSummoner-API-quarkus`. Leia o código e o `docs/adr/0001-decisoes-da-migracao-quarkus.md` de lá.

**Endpoints** (todos sob `/your-summoner`):

- `GET /champions/findVersion`
- `GET /champions/find-champion/{championName}`
- `GET /champions/by-mastery/{nick}/{tag}`
- `GET /champions/summoner/{nick}/tag/{tag}/lane/{lane}`
- `GET /matchs/{nick}/{tag}`
- `GET /matchs/{nick}/{tag}/history`
- `GET /matchs/matchId/{matchId}`
- `GET /summoners/{nick}/{tag}`
- Parâmetro de plataforma tratado pelo `PlatformResolver` (default `BR1`).

**Camadas:**

- `domain`: records de modelo (`Account`, `Champion`, `ChampionDetail`, `Match`, `PlayerMatch`, `SummonerProfile`, `Platform`, `Region`, `RiotId`, `Lane`...), exceções de domínio e `MasteryRanking`. Puro, sem Spring, sem Lombok, sem MapStruct.
- `application`: ports de entrada (`*UseCase`), ports de saída (`*Provider`) e services. Pode usar só anotação de estereótipo do Spring (`@Service`), o equivalente ao "só CDI" do Quarkus.
- `infrastructure`: adapters REST de entrada, adapters Feign de saída (Riot, Data Dragon, Community Dragon), adapters MongoDB, config, scheduler.

**Regras do ADR 0001 que continuam valendo:** (a) client da Riot por host de plataforma/região; (b) retry só em falha transitória e circuit breaker contando só indisponibilidade e timeout; (c) TTL de cache por tipo de dado, sem cachear "não encontrado"; (d) 429 repassado com `Retry-After`, sem rate limiter próprio; (e) erros RFC 9457 sem stack trace; (f) histórico de partidas em paralelo com no máximo 4 chamadas simultâneas, perfil em sequência; (g) chave só por `RIOT_API_KEY`, opcional na subida, 502 sem chave, e testes nunca podem receber a chave real; (h) semântica do endpoint de lane sem `chestGranted`; (j) chaves antigas no histórico já expiradas.

**Testes a portar:** unitários de domínio e services, testes de resource, WireMock da Riot (200/404/429/5xx/timeout), testes de cache, de resiliência e de tratamento de erro, smoke test e o `ArchitectureTest` (ArchUnit) travando as regras de camada.

## 6. Catálogo do Data Dragon no MongoDB

### 6.1 Coleções

| Coleção | Conteúdo | Chave |
|---|---|---|
| `patches` | Toda a lista de `versions.json`, com status da ingestão (`NOT_INGESTED`, `INGESTING`, `READY`, `FAILED`), data da ingestão e qual é o ativo | `version` |
| `champions` | Resumo de cada campeão no patch (de `champion.json`) | `{patch, locale, championId}` |
| `champion_details` | Detalhe de cada campeão no patch (`champion/{id}.json`) | `{patch, locale, championId}` |
| `champion_statistics` | Pick rate por lane do Community Dragon (`/latest/plugins/rcp-fe-lol-champion-statistics/...`). O Community Dragon só serve o `latest`, então o documento guarda o snapshot e o patch ativo na hora em que foi baixado | `patch` |

| `skins` | Uma skin por documento, vinda do `champion/{id}.json` do Data Dragon, já sem chromas (regra do commit `2f01414`), com `skinId`, `num`, nome e URLs de splash e loading | `{patch, locale, championId, num}` |

As skins também continuam dentro do `ChampionDetail` devolvido por `find-champion`, como no Quarkus. Endpoint novo: `GET /champions/{championName}/skins?patch=` (seção 6.4).

**Locale:** só `pt_BR`, vindo de `yoursummoner.locale`. Não existe `?locale=` nesta entrega. A chave guarda o locale para permitir outro sem migração. Trocar `yoursummoner.locale` (ex.: para `en_US`) e reiniciar tem que funcionar já nesta entrega: o patch ativo é ingerido no locale novo e os dados do locale antigo continuam no Mongo. Isso precisa de teste. A troca de idioma por requisição (`?locale=` ou `Accept-Language`) fica mapeada no roadmap como próxima atualização. Um patch está `READY` quando `champion.json` e todos os `champion/{id}.json` desse locale foram gravados.

O que é necessário para os endpoints atuais entra primeiro. Itens, runas e feitiços de invocador só entram quando algum endpoint usar (ver roadmap). Um documento por campeão, nunca o dicionário inteiro num documento só (limite de 16 MB do Mongo).

### 6.2 Fluxo de leitura

1. Requisição sem `?patch=` usa o patch ativo. Com `?patch=13.1.1`, usa esse.
2. Caffeine (L1) → MongoDB (L2) → Data Dragon.
3. Patch antigo que ainda não está no Mongo é ingerido na hora e gravado. Patch que não existe em `patches` responde 404 em RFC 9457.
4. Se o Data Dragon falhar e houver dado no Mongo, serve o Mongo. Isso mantém o fallback do último catálogo bom do Quarkus.
5. Se o Mongo cair em tempo de execução, a leitura vai direto ao Data Dragon (com Caffeine), com WARN no log. O Mongo é cache, não fonte da verdade.
6. Subida com Mongo vazio e Data Dragon fora: a API sobe mesmo assim. As rotas de catálogo respondem 503 em RFC 9457 até o job conseguir ingerir. As rotas da Riot seguem funcionando.

### 6.3 Troca automática de patch

- Um job agendado lê `versions.json`. Default: de hora em hora, configurável por `cron` em `application.yml`.
- Versão nova encontrada: grava em `patches`, ingere o patch inteiro e só **depois** marca como ativo. A troca é atômica do ponto de vista de quem consulta.
- Ingestão falhou: o patch anterior segue ativo, o novo fica `FAILED` e o job tenta de novo na próxima rodada.
- Na subida, se o Mongo já tem um patch `READY`, a API usa esse sem baixar nada. Isso substitui o `StaticDataWarmup` do Quarkus.
- Duas ingestões do mesmo patch não podem rodar ao mesmo tempo (status `INGESTING` como trava). Uma trava `INGESTING` com mais de 15 minutos é considerada abandonada (queda no meio da ingestão) e pode ser retomada.

### 6.4 Endpoints novos

- `GET /patches`: lista as versões conhecidas e qual é a ativa.
- `GET /champions/{championName}/skins?patch=`: skins do campeão no patch, com splash e loading.
- `?patch=` opcional nos endpoints de `/champions` que usam o catálogo (`find-champion`, `by-mastery`, lane). `findVersion` devolve o patch ativo.
- `/matchs` e `/summoners` não dependem do catálogo hoje (no Quarkus só `ChampionService` e `SummonerProfileService` usam `GameDataProvider`; o perfil usa o patch ativo). Eles não ganham `?patch=`.

### 6.5 Hexagonal

- Port de saída para o catálogo (`StaticDataRepository` ou nome equivalente), com adapter MongoDB.
- Port de saída para o Data Dragon (`GameDataProvider`, já existe no Quarkus), com adapter Feign.
- Caso de uso de sincronização de patch na `application`. O agendamento (`@Scheduled`) fica na `infrastructure` e só chama o caso de uso.
- Documentos do Mongo são classes da `infrastructure`, mapeadas para o domínio com MapStruct.

## 7. Base de conhecimento LoL e roadmap

`docs/lol/` deve cobrir, de forma curta e consultável:

- Cadência de patch: em geral quarta-feira, a cada 2 semanas, com exceções para hotfix de bug que quebra o jogo.
- O Data Dragon costuma publicar a versão nova horas depois do patch entrar no ar. Hotfix pequeno às vezes não gera versão nova no Data Dragon. Por isso o gatilho da troca é o `versions.json`, não o calendário.
- Diferença entre versão do jogo (`14.1`), versão do Data Dragon (`14.1.1`) e o nome comercial do patch.
- URLs do Data Dragon e do Community Dragon usadas pela API, e o que tem em cada arquivo.
- Roteamento da Riot API: plataforma (`br1`...) e região (`americas`...), qual endpoint usa qual.
- Rate limit e dev key que expira a cada 24h.
- Modos de jogo com regra própria, como Arena (`CHERRY`, subteams e colocação), e IDs de fila relevantes.

`docs/roadmap.md`: cronograma em fases, com o que entra agora e o que vem depois (ingestão em massa de patches antigos, diff entre patches, itens/runas/feitiços, rate limiter por host, OpenTelemetry, front).

## 8. Fluxo de trabalho no projeto

- `AGENTS.md` e `CLAUDE.md` passam a descrever a stack Spring e ganham a tabela de skills do superpowers do Mandato à Vista (brainstorming → writing-plans → executing-plans/subagent-driven-development, TDD, systematic-debugging, verification-before-completion, code review, finishing-a-development-branch).
- O ADR do Quarkus é mantido como histórico. Um ADR novo registra as decisões do port para Spring Boot 4.

## 9. Instalação do MongoDB

Regra do workspace: instalação portátil em `D:\Workspace\Languages-Frameworks`, com download verificado por checksum, igual ao `postgresql-16.14` e ao `pgdata-mandato`. Não há Docker na máquina. Instalação feita pelo agente `ops-runner`.

## 10. Calls do arquiteto (decididas por mim, não pelo usuário)

Revise estas:

1. **Branch:** `develop` local criada de `origin/develop` (que é o Spring Boot 2.6 de 2023). O trabalho vai em `feature/spring-boot-4-hexagonal`, a partir dela. A ideia é juntar em `develop` só quando você pedir.
2. **Concorrência:** virtual threads ligadas (`spring.threads.virtual.enabled`). O fan-out do histórico usa executor de virtual threads com limite de 4. `StructuredTaskScope` ainda é *preview* no Java 25, então só entra se você aceitar compilar com `--enable-preview`; sem isso, fica de fora. `ScopedValue` já é final no Java 25 e pode ser usado.
3. **Lombok** só na `infrastructure` e na `application` (construtor, logger). Records não precisam dele. `domain` fica sem Lombok para continuar puro.
4. **Resiliência:** Resilience4j via Spring Cloud CircuitBreaker, integrado ao OpenFeign, com as mesmas regras do ADR 0001 (b).
5. **Erros:** `ProblemDetail` nativo do Spring (RFC 9457), sem biblioteca extra.
6. **Cache L1:** Caffeine (`spring-boot-starter-cache`) com os mesmos TTLs do Quarkus para os dados da Riot.
7. **Testes do Mongo:** sem Docker, então Testcontainers não serve. Os testes de adapter Mongo usam MongoDB embarcado (flapdoodle) ou o Mongo portátil local. Unitários mocam o port.
8. **MongoDB:** Community Server 8.x portátil + `mongosh`, dados em `D:\Workspace\Languages-Frameworks\mongodata-yoursummoner`, só em `127.0.0.1`. URI por variável de ambiente `MONGODB_URI`, com default local.
9. **Job de patch:** de hora em hora, todo dia.
10. **Código de 2023 no `develop`:** é substituído por inteiro pelo layout hexagonal. Nada dele é mantido além do histórico git; o comportamento de referência é o do Quarkus.
11. **Versões:** se o release train do Spring Cloud ainda não suportar o Boot 4.x mais novo, fixa-se o Boot mais novo que ele suporta (sempre ≥ 4.0). Não se troca o OpenFeign por HTTP Interface.
12. **Locale e degradação:** só `pt_BR`; Mongo fora vira leitura direta do Data Dragon; subida sem catálogo responde 503 nas rotas de catálogo; trava de ingestão expira em 15 min (seção 6).
13. **Delegação:** Codex (`gpt-5.6-sol`) implementa as tarefas com lógica; Gemini (FreeLLMAPI) faz leitura em massa do Quarkus e rascunho de docs; Ollama (`qwen3:14b`) gera boilerplate de records/DTOs; o Opus escreve as especificações e revisa todo diff.

## 11. O que fica com o builder

Nomes exatos de classes e pacotes novos, versão exata das dependências, formato dos documentos Mongo além das chaves, estrutura interna de `docs/lol/`, ordem das tarefas dentro de cada fase.

## 12. Administração de parâmetros e segredos (adendo de 2026-11-06, entra nesta entrega)

O usuário quer cadastrar chaves de API e parâmetros numa tela de administrador, sem editar código nem `.env`, mantendo a aplicação segura. Pedido explícito: entra agora, não no roadmap.

### 12.1 O que é administrável

- **Segredos:** começa com `RIOT_API_KEY`. Novos segredos entram como novos valores de um enum `SecretName`.
- **Parâmetros:** `locale` e `defaultPlatform`. Valem a quente, sem reiniciar. O `cron` do patch continua no `application.yml` (call do arquiteto: reagendar cron em tempo de execução não compensa agora).

### 12.2 Regras de segurança

- Segredos ficam na coleção `secrets`, **sempre criptografados** com AES-256-GCM (IV aleatório por gravação). A chave mestra vem só de `YOURSUMMONER_MASTER_KEY` (32 bytes em Base64), nunca do banco. Sem chave mestra, a área de admin fica desligada e a API usa só as variáveis de ambiente.
- A API **nunca devolve** o valor de um segredo. Só nome, 4 últimos caracteres, data e autor da última troca.
- Precedência da chave da Riot: valor do cofre, se existir; senão `RIOT_API_KEY`. A troca vale na próxima chamada, sem reiniciar.
- Acesso: Spring Security. As rotas públicas atuais continuam abertas. `/admin/**` exige papel `ADMIN`.
- Login: `POST /admin/session` com usuário, senha e código TOTP (2FA). Responde um JWT curto (15 min, HS256, chave derivada da chave mestra). As demais rotas `/admin/**` exigem `Authorization: Bearer`.
- Usuário admin único, definido por `YOURSUMMONER_ADMIN_USER` e `YOURSUMMONER_ADMIN_PASSWORD_HASH` (BCrypt). O segredo TOTP é gerado no primeiro login bem-sucedido, devolvido uma única vez como `otpauth://` e gravado criptografado; dali em diante o código é obrigatório.
- Rate limit no login: 5 tentativas por 15 min por IP. Falhas viram 401 genérico, sem dizer qual campo errou.
- Por padrão, `/admin/**` só aceita requisição de `127.0.0.1`/`::1` (`yoursummoner.admin.allowed-cidrs`).
- Auditoria na coleção `admin_audit`: quem, quando, o quê, IP. Nunca o valor.
- Teste garante que nenhum segredo aparece em log, resposta, `toString` ou mensagem de erro.

### 12.3 Hexagonal

- `domain`: `SecretName`, `SecretMetadata`, `AppSettings`.
- `application`: ports de saída `SecretStore`, `SettingsStore`, `AuditLog`; port `RiotCredentials` (`Optional<String> riotApiKey()`), usado pelo interceptor do Feign; `CredentialService` (cofre com fallback para o ambiente); casos de uso `ManageSecretsUseCase`, `ManageSettingsUseCase`.
- `infrastructure`: adapter Mongo que criptografa ao gravar e decifra ao ler (`AesGcmCipher`), Spring Security, controller `/admin/**`, bean de fallback `EnvRiotCredentials` (só a variável de ambiente) enquanto o cofre não existir.

### 12.4 Tela

A tela fica no `YourSummoner-Web` (Go, `html/template`), em `/admin`: login com TOTP, lista de segredos (mascarados) com campo só de gravação, edição de `locale` e `defaultPlatform`, e o log de auditoria. O JWT fica num cookie `HttpOnly`, `SameSite=Strict`, e o formulário tem token CSRF. O Web chama a API; nunca guarda segredo.

### 12.5 Fora desta entrega (roadmap)

Cofre externo (Vault/Infisical), vários usuários admin e papéis, rotação automática da chave mestra.
