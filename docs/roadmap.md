# Roadmap — YourSummoner-API (Spring Boot 4)

Cronograma de evolução da API, estruturado em fases incrementais.

## Fase 1 — Port para Spring Boot 4 e Catálogo Persistido (Em andamento)
- [ ] Arquitetura hexagonal purificada em Java 25.
- [ ] Port completo dos endpoints, regras de negócio e resiliência do Quarkus para Spring Boot 4.
- [ ] Persistência do catálogo do Data Dragon no MongoDB (coleções `patches`, `champions`, `champion_details`, `champion_statistics`, `skins`).
- [ ] Job de hora em hora para verificação de novas versões (`versions.json`) e troca automática e atômica de patch.
- [ ] Endpoint de listagem de patches (`GET /patches`) e consulta de skins (`GET /champions/{championName}/skins?patch=`).
- [ ] Testes unitários, de integração, WireMock e ArchUnit.

## Fase 2 — Internacionalização por Requisição
- [ ] Suporte a parâmetro `?locale=` e header `Accept-Language` nos endpoints de campeões e catálogo.
- [ ] Ingestão sob demanda de locales adicionais no MongoDB.

## Fase 3 — Histórico e Análise Avançada de Patches
- [ ] Ingestão em massa de patches antigos e armazenamento histórico.
- [ ] Comparativo de diff entre patches por campeão (buffs e nerfs estruturados).

## Fase 4 — Expansão do Catálogo Estático
- [ ] Inclusão de itens, runas e feitiços de invocador no MongoDB e exposição em novos endpoints.

## Fase 5 — Resiliência Avançada e Observabilidade
- [ ] Rate limiter próprio por host da Riot.
- [ ] Integração com OpenTelemetry e métricas avançadas (Micrometer).

## Fase 6 — Integração Front-End
- [ ] Homologação completa com o front-end YourSummoner-Web (Angular / React).
- [ ] Ajustes de CORS, contratos e performance em produção.
