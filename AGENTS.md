# YourSummoner-API

Spring Boot 4, Java 25 e arquitetura hexagonal. O `ArchitectureTest` trava: `domain` puro; `application` só usa estereótipos Spring; adapters e configuração ficam em `infrastructure`.

Stack: Spring Cloud OpenFeign, Resilience4j, MongoDB, Caffeine, springdoc, MapStruct/Lombok, JUnit/Mockito/WireMock/JaCoCo. Segredo somente em `RIOT_API_KEY`; Mongo somente em `MONGODB_URI`.

Comandos: `mvn spring-boot:run`, `mvn test`, `mvn verify`.

Documentação: [ADRs](docs/adr/), [LoL](docs/lol/) e [roadmap](docs/roadmap.md).

## Regras

- Nunca commite chaves, `.env` ou segredos. A chave da Riot entra só por `RIOT_API_KEY` (propriedade `riot.api.token`, nunca `riot.api.key`).
- A versão Quarkus fica na branch local `feature/quarkus-hexagonal` como referência; não dê push nela.
- A sessão principal planeja e revisa; implementação vai para os delegados (Codex, Gemini via FreeLLMAPI, Ollama local, subagentes), com spec fechada e revisão do diff.
- Testes no Windows: o surefire passa `-Djdk.net.unixdomain.tmpdir=${project.build.directory}`; sem isso o `HttpClient` do JDK falha com "Unable to establish loopback connection".

## Development method: superpowers

Development here follows the [superpowers](https://github.com/obra/superpowers) skills. Before acting on a task, invoke the skill that fits it:

| When | Skill |
|---|---|
| A new feature, a change of behavior or a design decision | `superpowers:brainstorming`, then `superpowers:writing-plans` |
| Running a written plan | `superpowers:executing-plans`, or `superpowers:subagent-driven-development` for independent tasks |
| Any feature or bug fix | `superpowers:test-driven-development` |
| A bug, a failing test or behavior you did not expect | `superpowers:systematic-debugging` |
| Two or more tasks that share no state | `superpowers:dispatching-parallel-agents` |
| Work that needs its own workspace | `superpowers:using-git-worktrees` |
| Before saying the work is done, passes or is ready to commit | `superpowers:verification-before-completion` |
| After a task or before a merge, and when review comments arrive | `superpowers:requesting-code-review`, `superpowers:receiving-code-review` |
| The branch is complete | `superpowers:finishing-a-development-branch` |

## Ferramentas do workspace

Scripts de rotina em `D:\Workspace\Scripts\yoursummoner` (ver `README.md` lá): `verify.ps1 -Repo <repo>` (build e testes com resumo), `stack.ps1 up|down|status` (Mongo, API, coletor, processor), `collector-check.ps1`, `board.ps1` (quadro). JDK 25 em `D:\Workspace\Languages-Frameworks\jdk-25.0.4.1+1`, Maven em `D:\Workspace\Languages-Frameworkspache-maven-3.10.0`. Agente delegado não roda git.
