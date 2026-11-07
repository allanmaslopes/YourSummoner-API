# ADR 0002 — Port para Spring Boot 4 e Persistência do Catálogo no MongoDB

- Status: aceita
- Data: 2026-11-06
- Branch: `feature/spring-boot-4-hexagonal`

## Contexto

A API foi reescrita em Quarkus 3.40.1 / Java 25. O usuário decidiu retornar ao ecossistema Spring Boot, adotando a versão mais recente (Spring Boot 4.x), mantendo rigorosamente todas as regras de negócio, contratos, resiliência e arquitetura hexagonal consolidadas no Quarkus. Adicionalmente, para otimizar o desempenho e evitar downloads recorrentes dos pesados dicionários estáticos do Data Dragon, o catálogo passa a ser persistido no MongoDB, com suporte à troca automática de patches.

## Decisões

### (a) Stack de Tecnologia e Frameworks
- **Java 25:** Utilização de records, text blocks e virtual threads para fan-out no histórico de partidas.
- **Spring Boot 4.x:** Framework principal, com Jakarta Validation e ProblemDetail nativo (RFC 9457).
- **Spring Cloud OpenFeign:** Mantido por escolha explícita do usuário para os clients REST externos, mesmo estando em modo de manutenção no Spring.
- **Persistência:** Spring Data MongoDB com MongoDB portátil (v8.x), servindo como cache persistente (L2) para o catálogo estático, com Caffeine (L1) em memória na frente.

### (b) Concorrência e Virtual Threads
- Virtual threads habilitadas (`spring.threads.virtual.enabled=true`).
- O fan-out do histórico de partidas utiliza executor de virtual threads com limite de concorrência (`Semaphore` de 4 threads) para respeitar o rate limit da dev key da Riot.
- `StructuredTaskScope` não é utilizado por permanecer em modo *preview* no Java 25.

### (c) Estrutura de Camadas e Lombok
- **Domínio (`domain/`):** Puramente em Java moderno (records, enums), sem nenhuma anotação de framework, Spring ou Lombok.
- **Aplicação (`application/`):** Casos de uso (`*UseCase`) e serviços, utilizando anotações de estereótipo do Spring (`@Service`).
- **Infraestrutura (`infrastructure/`):** Adapters REST (Feign), adapters MongoDB, mappers (MapStruct), configurações e schedulers. Lombok é permitido apenas aqui e na aplicação para construtores, getters e loggers.

### (d) Resiliência e Tolerância a Falhas
- Utilização de Resilience4j via Spring Cloud CircuitBreaker, integrado ao OpenFeign.
- O circuit breaker conta exclusivamente indisponibilidades e timeouts, com retry em falhas transitórias.

### (e) Catálogo e Troca Automática de Patches
- Coleções no MongoDB (`patches`, `champions`, `champion_details`, `champion_statistics`, `skins`).
- Job agendado de hora em hora verifica o `versions.json` do Data Dragon. Novas versões são ingeridas atomicamente e marcadas como ativas (`READY`).
- Fallback robusto: se o Data Dragon falhar, serve-se o MongoDB. Se o MongoDB cair, a leitura recorre diretamente ao Data Dragon com Caffeine.

### (f) Tratamento de Erros e Contratos
- Resposta de erros no formato RFC 9457 (`application/problem+json`) via `ProblemDetail` nativo do Spring, com `include-details=false`.
- Manutenção integral dos contratos HTTP, parâmetros de plataforma (`PlatformResolver`), tratamento de Riot ID em caixa baixa e repasse de 429 com `Retry-After`.

### (g) Chave da Riot e Segurança
- A chave da API da Riot é obtida exclusivamente da variável de ambiente `RIOT_API_KEY`.
- Ausência de chave na subida não impede a inicialização, mas rotas da Riot respondem 502. Testes automatizados nunca recebem a chave real.

### (h) Restrições e Suposições
- O Community Dragon fornece estatísticas exclusivamente no path `latest`, sendo capturado como snapshot no momento da ingestão do patch.
- O idioma padrão é `pt_BR`, configurável via `yoursummoner.locale`.

## Consequências

- O sistema ganha alta performance e independência de downloads repetidos ao CDN do Data Dragon graças ao MongoDB.
- A troca automática de patches ocorre sem impacto ou interrupção para os consumidores da API.
- O ecossistema Spring Boot 4 integrado ao Java 25 oferece a robustez desejada mantendo a paridade funcional com a versão Quarkus.
- As regras arquiteturais (arquitetura hexagonal) continuam garantidas por testes automatizados (ArchUnit).
- As regras (a) a (j) do ADR 0001 continuam integralmente válidas para o comportamento de integração e domínio.
