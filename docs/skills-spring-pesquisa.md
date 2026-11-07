# Pesquisa: skills do Claude Code para Spring Boot / Java

Data da pesquisa: 2026-11-07. Escopo: projeto Spring Boot 4.1 + Java 25, hexagonal, OpenFeign, Resilience4j, MongoDB, MapStruct/Lombok, JUnit 5/Mockito/WireMock/ArchUnit/JaCoCo.

Método: pesquisa na web e leitura do `SKILL.md` (via `raw.githubusercontent.com` quando disponível). Nada foi baixado, instalado nem executado. Campos marcados como "não verificado" não apareceram na fonte consultada.

## Tabela de skills

| Skill | URL | Autor / mantenedor | O que faz | Última atualização | Estrelas | Licença | Compatibilidade (Boot 3/4, Java 21+) | Risco |
|---|---|---|---|---|---|---|---|---|
| `spring-boot-testing` | https://github.com/github/awesome-copilot/tree/main/skills/spring-boot-testing | GitHub (repo `github/awesome-copilot`) | Escolhe a técnica de teste: test slices (`@WebMvcTest`, `@DataJpaTest`, `@RestClientTest`), Testcontainers com `@ServiceConnection`, Failsafe, `@MockitoBean`, AssertJ, JaCoCo (meta de 80%). | Não verificada (página não mostrou data) | Repo: ~40,9 mil | MIT (repo) | Escrita para Boot 4, com notas para Boot 3. Java não declarado no frontmatter. Não cita Mongo nem WireMock. | Baixo. Manda rodar `./mvnw verify` e sugere plugins Failsafe/JaCoCo no `pom.xml`. Não baixa nada fora do build. |
| `java-springboot` | https://github.com/github/awesome-copilot/tree/main/skills/java-springboot | GitHub (repo `github/awesome-copilot`) | Escreve e revisa código em projeto Spring Boot 3/4 existente: controllers, ProblemDetail, validação, `RestClient`, `@ConfigurationProperties`, cache, virtual threads. Detecta versões antes de editar, não faz upgrade implícito e pede confirmação antes de criar infraestrutura. | Não verificada | Repo: ~40,9 mil | MIT (repo) | Boot 3 e 4. Java não declarado. Aponta para a skill de scaffold quando for projeto novo. | Baixo. Manda rodar `./mvnw verify` ou `./gradlew build` e adicionar dependências só se o usuário pedir. Sem URLs externas. |
| `create-spring-boot-java-project` (mesmo repo) | https://github.com/github/awesome-copilot/tree/main/skills (procurar pelo nome) | GitHub | Cria projeto novo. | Não verificada | Repo: ~40,9 mil | MIT (repo) | Não verificado | Médio. Segundo resumo de terceiro, consulta `start.spring.io` em tempo de execução. Não é para projeto existente. Não recomendada. |
| `spring-boot` (`claude-ai-spring-boot`) | https://github.com/piomin/claude-ai-spring-boot | Piomin (autor conhecido da comunidade Spring; identidade não conferida além do repo) | Guia de Spring Boot 4.x: REST, JPA, segurança, testes, cloud, Problem Details, Flyway. Fluxo analisar, projetar, implementar, testar. | Não verificada (repo lista 78 commits, sem data) | ~1,4 mil | Apache-2.0 | Boot 4.x, Java 21+ (declarado no README do repo). | Baixo-médio. Usa `./mvnw clean verify`. Repo traz skill `conventions` com "no Lombok" e "constructor injection": conflita com o projeto (usa Lombok e MapStruct). Usar só a skill `spring-boot`. |
| `testing-patterns` (mesmo repo) | https://github.com/piomin/claude-ai-spring-boot | Piomin | JUnit 5, Mockito, MockMvc, `RestTestClient`, Testcontainers. | Não verificada | ~1,4 mil | Apache-2.0 | Boot 4.x, Java 21+ | Baixo. Mesmo cuidado com o Lombok. |
| `code-quality` (mesmo repo) | https://github.com/piomin/claude-ai-spring-boot | Piomin | Checklist de revisão: correção, segurança, desempenho, code smells. | Não verificada | ~1,4 mil | Apache-2.0 | Genérica (Java 21+) | Baixo. |
| `clean-architecture` | https://github.com/giuseppe-trisciuoglio/developer-kit (skill `clean-architecture`) | Giuseppe Trisciuoglio | Clean Architecture, hexagonal e DDD para Java e Spring Boot. | Não verificada (repo lista 619 commits, sem data) | ~367 | MIT | Java 21+ e Boot 3.5+ segundo resumo de terceiro. `SKILL.md` não lido (URL raw deu 404). | Médio, a confirmar. Autor individual, `SKILL.md` não verificado. Ler o arquivo antes de instalar. |
| `hexagonal-java` | https://github.com/marvinrichter/clarc/blob/HEAD/skills/hexagonal-java/SKILL.md | Marvin Richter (autor individual) | Hexagonal em Java: estrutura de pacotes, ports, use cases, adapters, testes. | 2026-05-21 (segundo agregador claudskills.com) | Não mostrado | Não verificada | Java Spring Boot, sem versão declarada. | Médio. Instalação manual via `curl` (passo do usuário). `SKILL.md` não lido (URL raw deu 404). Ler antes de usar. |
| `java-ddd-hexagonal` | https://openskillindex.com/skills/majiayu000-claude-skill-registry-java-ddd-hexagonal (origem: github.com/majiayu000/claude-skill-registry) | majiayu000 (registro comunitário, agregador) | DDD e ports-and-adapters para Java/Spring Boot, TDD red-green-refactor. | Não verificada | Não verificado | Não verificada | Java 17+. | Alto-médio. Autor e repo de registro comunitário. Não lido. |
| `java-spring-boot` | https://github.com/pluginagentmarketplace/custom-plugin-java | pluginagentmarketplace (Dr. Umit Kacar e Muhsin Elcicek) | REST, segurança, dados, Actuator. | Não verificada (README: "Last Updated 2025-12-30") | 42 | Licença própria ("Custom License") | Spring Boot 3.x; default `spring_version: "3.2"`. Java 17+ por inferência. | Médio. Frontmatter tem `allowed-tools: Read, Write, Bash, Glob, Grep`, ou seja, pode rodar shell. Licença restritiva e pouca tração. |
| `anthropics/skills` (repo oficial) | https://github.com/anthropics/skills | Anthropic | Repositório oficial de skills (docx, pdf, pptx, xlsx, exemplos, criação de MCP). | Não verificada | ~188 mil | Apache-2.0 em partes; conferir LICENSE de cada pasta | Não aplicável: não encontrei skill de Spring/Java/JUnit. | Baixo (oficial), mas sem skill Spring. |

## Top 5 recomendado para este projeto

1. **`spring-boot-testing` (github/awesome-copilot).** Cobre a parte de testes do projeto (test slices, Failsafe para integração, JaCoCo, `@MockitoBean`), com Boot 4. Não tem Mongo nem WireMock: complementar com o próprio projeto e com a documentação do Testcontainers. Risco baixo.
2. **`java-springboot` (github/awesome-copilot).** Serve para a rotina de feature e revisão em projeto existente: ProblemDetail, `RestClient`, configuração tipada, e a regra de não fazer upgrade implícito, que combina com o `CLAUDE.md` do projeto. Risco baixo.
3. **`spring-boot` (piomin/claude-ai-spring-boot).** Referência para Boot 4 e Java 21+, com licença Apache-2.0 e autor conhecido. Usar **somente** a skill `spring-boot`. Não instalar `conventions` (contradiz Lombok e MapStruct do projeto).
4. **`clean-architecture` (giuseppe-trisciuoglio/developer-kit).** Cobre hexagonal e DDD em Java 21+, MIT, 367 estrelas. Condição: ler o `SKILL.md` antes de instalar, porque não consegui abri-lo.
5. **`hexagonal-java` (marvinrichter/clarc).** Específica de hexagonal. Condição: revisar o `SKILL.md` e não usá-la se houver conflito com o `ArchitectureTest` e as regras do projeto (domínio puro, estereótipos Spring na aplicação).

Observação sobre o projeto: nenhuma skill cobre Mongo, OpenFeign, Resilience4j ou WireMock de forma específica. Para esses, o caminho mais seguro é `context7` (documentação oficial), não skill de terceiros.

## Não recomendadas

- **`create-spring-boot-java-project` (awesome-copilot).** Feita para projeto novo; o projeto já existe. Consulta `start.spring.io` em tempo de execução segundo resumo de terceiro, o que não é necessário aqui.
- **`conventions` (piomin/claude-ai-spring-boot).** Impõe "no Lombok" e contradiz o projeto, que usa Lombok e MapStruct.
- **`java-spring-boot` (pluginagentmarketplace/custom-plugin-java).** Alvo é Spring Boot 3.2, licença própria restritiva, 42 estrelas, e o frontmatter concede `Bash`. Não compensa frente às opções acima.
- **`java-ddd-hexagonal` (registro majiayu000).** Autor desconhecido, em agregador comunitário, não lido.
- **`anthropics/skills`.** Oficial e confiável, mas sem skill de Spring, Java ou JUnit. Útil só como referência de formato `SKILL.md`.
- **Agregadores (claudemarketplaces.com, claudskills.com, skillstore.io, vibeindex.ai).** Contagens de estrelas e instalações não conferidas. Usar como índice, não como fonte de confiança.

## Fontes

- Busca e páginas consultadas: [GitHub anthropics/skills](https://github.com/anthropics/skills), [awesome-copilot spring-boot-testing](https://github.com/github/awesome-copilot/tree/main/skills/spring-boot-testing), [awesome-copilot java-springboot](https://github.com/github/awesome-copilot/tree/main/skills/java-springboot), [awesome-copilot (repo)](https://github.com/github/awesome-copilot), [piomin/claude-ai-spring-boot](https://github.com/piomin/claude-ai-spring-boot), [pluginagentmarketplace/custom-plugin-java](https://github.com/pluginagentmarketplace/custom-plugin-java), [giuseppe-trisciuoglio/developer-kit](https://github.com/giuseppe-trisciuoglio/developer-kit), [claudskills.com hexagonal-java](https://claudskills.com/skills/hexagonal-java/), [marvinrichter/clarc hexagonal-java](https://github.com/marvinrichter/clarc/blob/HEAD/skills/hexagonal-java/SKILL.md) (URL raw indisponível), [openskillindex java-ddd-hexagonal](https://openskillindex.com/skills/majiayu000-claude-skill-registry-java-ddd-hexagonal).
- Agregadores (índice, não verificados): [claudemarketplaces.com awesome-copilot/create-spring-boot-java-project](https://claudemarketplaces.com/skills/github/awesome-copilot/create-spring-boot-java-project), [claudemarketplaces.com java](https://claudemarketplaces.com/skills/category/java), [besthub.dev, 8 skills para Spring Boot](https://www.besthub.dev/articles/8-essential-claude-code-skills-to-supercharge-your-spring-boot-projects-aea5502621c1), [Medium, Java com skills](https://lasithaben.medium.com/supercharge-your-java-development-with-claude-code-skills-da75e2efca2b).

## Lacunas

- Datas de última atualização não apareceram nas páginas do GitHub consultadas, exceto onde indicado.
- Não consultei o Context7 nem plugins oficiais da Anthropic para Spring. A busca pelo Context7 não foi feita.
- `SKILL.md` de `clean-architecture` e de `hexagonal-java` não foram lidos (URL raw deu 404). Antes de instalar qualquer uma, ler o arquivo.
