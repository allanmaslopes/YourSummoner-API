# Roadmap — YourSummoner-API (Spring Boot 4)

Cronograma de evolução da API, estruturado em fases incrementais.

## Fase 1 — Port para Spring Boot 4 e Catálogo Persistido (Em andamento)
- [ ] Arquitetura hexagonal purificada em Java 25.
- [ ] Port completo dos endpoints, regras de negócio e resiliência do Quarkus para Spring Boot 4.
- [ ] Persistência do catálogo do Data Dragon no MongoDB (coleções `patches`, `champions`, `champion_details`, `champion_statistics`, `skins`).
- [ ] Job de hora em hora para verificação de novas versões (`versions.json`) e troca automática e atômica de patch.
- [ ] Endpoint de listagem de patches (`GET /patches`) e consulta de skins (`GET /champions/{championName}/skins?patch=`).
- [ ] Testes unitários, de integração, WireMock e ArchUnit.
- [ ] Administração de segredos e parâmetros: cofre AES-256-GCM no MongoDB, login com senha + TOTP, JWT de 15 min, tela `/admin` no YourSummoner-Web.

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
- [ ] Homologação completa com o front-end YourSummoner-Web (Go, `html/template`).
- [ ] Ajustes de CORS, contratos e performance em produção.

## Fases 7 a 19 — Funcionalidades inspiradas em OP.GG, Mobalytics e Blitz.gg

Detalhes, fontes e restrições da Riot em [produto/benchmark-concorrentes.md](produto/benchmark-concorrentes.md).

| Fase | Entrega | Persiste dados de jogador? | Esforço |
|---|---|---|---|
| F7 | Multi-busca (até 5 Riot IDs) | Não | P |
| F8 | Estatísticas do jogador por campeão (últimas N partidas) | Não | P |
| F9 | Partida ao vivo (`spectator-v5`) | Não | M |
| F10 | Timeline da partida (ouro/XP, ordem de skills e itens) | Não | M |
| F11 | Leaderboards (Challenger/GM/Master) | Não (cache) | M |
| F12 | Painel de mudanças do patch (diff do Data Dragon) | Não | M |
| F13 | Mini-GPI pessoal | Não | M |
| F14 | Desafios (`lol-challenges-v1`) | Não | P |
| F15 | Status do servidor e Clash | Não | P |
| F16 | Histórico de PdL opt-in | **Sim** | M |
| F17 | Agregação de meta (win/pick/ban por campeão, elo e patch) | **Sim** (anônimo) | G |
| F18 | Tier list, builds, matchups e skill order de meta | Lê F17 | M |
| F19 | GPI com baseline por elo | Lê F17 | M |

**Próximo passo recomendado:** F7, F8 e F9 (nenhuma exige rever a decisão de não guardar dados de jogador). F16, F17 e F19 exigem rever essa decisão antes.
