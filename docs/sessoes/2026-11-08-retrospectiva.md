# Retrospectiva das sessões de 07 e 08/11/2026

## O que foi entregue

| Repositório | Commits | Principais entregas |
|---|---|---|
| YourSummoner-API | 9 | Port Spring Boot 4 levado ao `main` (PR #26), CI no GitHub, correções de CI no Linux (ordem dos handlers, Mongo embarcado nos testes), alerta do `org.json` fechado |
| yoursummoner_collector | 5 | Fragmentos de runas (`statPerks`), CI |
| yoursummoner_processor | 18 | Processor v1, runas/feitiços/itens por runa principal, pacote v2 sempre com recomendação, CI |
| live-builds | 15 | Bugs #5 (bota da missão) e #15 (Calçados Mágicos), painel de elo alto (#1, #12), seleção de campeões pela LCU (Tasks 1–7: conexão, runas, páginas, arsenais, aplicação automática, lembrete, telas e editores), runas ao escolher o campeão e 3 opções de runa |

Pendente com o usuário: teste real da seleção de campeões (Task 8). A escrita no cliente já foi confirmada (página `live-builds`, arsenal, sets do usuário intactos).

## Scripts usados (`D:\Workspace\Scripts\yoursummoner`)

| Script | Uso hoje |
|---|---|
| `delegate.ps1` | 25 execuções do Codex registradas (20 concluídas, 5 cortadas por cota); padrão Terra/medium |
| `review.ps1` | 9 revisões linha a linha pelo Codex somente leitura |
| `verify.ps1` | build e testes de todos os repositórios antes de cada commit |
| `stack.ps1` | subir/derrubar Mongo, API, coletor e processor; passou a criar os processos pelo WMI (sobrevivem ao reinício do app) |
| `board.ps1` | 8 issues criadas e 8 fechadas no quadro |
| `collector-check.ps1` | contagem de partidas, tamanho médio e anonimato |
| `ci-init.ps1` | workflows do GitHub Actions nos 4 repositórios |
| `test-scripts.ps1` | autoteste dos scripts (44 checagens) |
| `measure.ps1`, `m2-sync.ps1`, `admin-setup.ps1` | não usados hoje |
| `examples/lcu_probe.rs` (live-builds) | sonda somente leitura do cliente do LoL |

## Codex: quanto economizou

Medição pelos logs (`logs/delegate-codex-*.out`) e pelos tokens informados nas execuções do Claude:

- **Codex:** cerca de 26 execuções úteis (20 registradas pelo `delegate.ps1` + ~6 do processor pela manhã), sendo ~17 implementações/correções e ~9 revisões ou checagens de design.
- **Custo equivalente no Claude:** uma implementação no Sonnet custou ~125 mil tokens e uma revisão ~140 mil. As execuções do Codex evitaram algo como **3,4 milhões de tokens do Claude** (17 × 125 mil + 9 × 140 mil).
- **Claude usado como reserva:** ~16 execuções do Sonnet (~2 milhões de tokens) e ~8 revisões (~1,1 milhão), quase todas porque a cota do Codex acabou.
- **Cota do Codex:** acabou 5 vezes. Com o Sol, cada janela de cota rendeu 5 a 8 execuções; o Terra rendeu mais.

Conclusão: o Codex fez cerca de metade do trabalho delegado. Ele só não fez mais por cota.

## Aprendizado

1. **Modo enxuto do Codex funciona.** Sem os 17 plugins e 3 MCPs, com rede e `~/.m2`/`~/.embedmongo` no sandbox, ele roda Maven, Mongo embarcado e Cargo sozinho. O `~/.codex/AGENTS.md` (cópia das regras do Claude) precisa do aviso "você é o implementador".
2. **O Sol gasta a cota rápido.** Revisões em série no Sol foram o maior consumo. O Terra deu conta de várias tarefas (Tasks 1, 2 e 5, correções de revisão).
3. **As revisões pagam o preço.** Acharam 3 casos de perda de dados nos arsenais do usuário, um fallback ausente e builds ordenadas errado. Mas 2 ou 3 rodadas por task dobram o custo.
4. **O cliente real muda o desenho.** A sonda mostrou o LoL em `D:\`, o Blitz importando runas, o inventory no lugar do `customizationlimits` e a seleção de ~10 s com bots. Esses dados viraram regra antes de gravar qualquer coisa.
5. **Quedas de luz.** O git e o Mongo resistiram (fsck e validate limpos), mas trabalho em andamento do delegado se perdeu duas vezes. Os serviços morriam com o reinício do app até o `stack.ps1` passar a usar WMI.
6. **Dados da Riot.** A recomendação de runas da Riot (`ChampionRuneRecommendationsContext`) está vazia no 16.22, e 54 campeões não têm build oficial: o elo alto virou a fonte principal.

## Melhorias propostas

1. **Codex:** Terra por padrão também na revisão; Sol só na primeira implementação de lógica difícil e na revisão de código que grava dados do usuário.
2. **Especificações:** colocar o "Review Focus" e as regras de preservação de dados na especificação, para reduzir rodadas de revisão. Passar trechos de código e caminhos exatos, para o delegado não explorar o repositório.
3. **Uma rodada de revisão por task**, com os achados LOW indo direto para issue.
4. **Gemini e Ollama** ficaram parados hoje: usar para checagem de design, rascunhos e fixtures, que gastam pouco raciocínio.
5. **Commits menores e mais cedo** nas tasks grandes, por causa das quedas de luz.
6. **Medir por cota:** o `delegate.ps1` pode registrar modelo, duração e resultado numa linha de CSV para a medição semanal.
7. **Sonda:** parar de imprimir o `summonerId` na linha "consultando".

## Próxima sessão

Agendado para 08/11 às 06h07 (sem o usuário):
1. processor#8: build completa até o 6º item (partidas com 5+ itens).
2. live-builds: painel e arsenal com a build completa.
3. Rascunho de design do motor de recomendação durante a partida (para discutir).

Com o usuário:
1. Teste real da seleção de campeões (Task 8): chaves desligadas e "Aplicar", chaves ligadas, troca de campeão, 3 opções de runa, editores, limite de páginas, feitiços. Desligar o Blitz antes.
2. Discutir o rascunho do motor e decidir a primeira entrega dele.
3. Entrega 2 da seleção: explicar cada troca de runa com partidas e vitória.
4. Entrega 3: sugerir campeão pelo que o time precisa.
5. Push dos commits locais (live-builds, processor, coletor, API) quando o usuário autorizar.
6. Backlog: live-builds#14 (item de grau superior), #16 (LOW do painel), processor#7, collector#9 (Diamante+, opcional).
