# Gerenciamento: delegação, quadro e contexto para agentes

Data: 2026-11-08. Complementa `2026-11-08-retrospectiva.md`.

## 1. Modelos do Codex (pesquisa)

Fontes: [preços do Codex](https://chatgpt.com/codex/pricing/) (oficial), issues do repositório `openai/codex` ([#41468](https://github.com/openai/codex/issues/41468), [#41518](https://github.com/openai/codex/issues/41518), [#47051](https://github.com/openai/codex/issues/47051)), [guia de configuração](https://codex.danielvaughan.com/2026/06/12/codex-cli-configuration-anti-patterns-twelve-settings-mistakes-tokens-sandbox-agent-performance/) e [controle de saída](https://codex.danielvaughan.com/2026/05/02/codex-cli-output-control-verbosity-reasoning-summaries-token-budgets/) (terceiros).

| Modelo | Limite no Plus por 5 h (oficial) | Uso indicado aqui |
|---|---|---|
| `gpt-5.6-luna` | 250–2.000 | tarefas mecânicas: fixtures, docs, renomear, scripts simples, checagem de design, revisão de diffs pequenos |
| `gpt-5.6-terra` | não informado (entre os dois) | **padrão**: implementação com spec fechada, correções de revisão, revisão de diffs médios |
| `gpt-5.6-sol` | 10–100 | só primeira implementação de lógica difícil e revisão de código que grava dados do usuário; esforço `medium`, nunca `high`/`max` sem pedido |
| `gpt-6-astra` | — | proibido (regra do usuário) |

Relatos (não oficiais): uma tarefa no Sol High gastou ~60% da janela; um prompt de ~15 mil caracteres no Sol Max esgotou a janela. O consumo cresce com o tamanho do prompt, o esforço e o número de chamadas de ferramenta.

## 2. O que mudar na delegação

1. **Escada de modelos:** Luna → Terra → Sol. Subir só quando a revisão reprovar. `delegate.ps1` já usa Terra/medium por padrão; acrescentar `-To codex -Model luna` nas tarefas mecânicas.
2. **Prompt curto:** a spec cita trechos e caminhos exatos; nada de "leia o plano inteiro" quando só uma task importa (recortar a task para o arquivo da tarefa).
3. **AGENTS.md global do Codex (feito em 08/11, decisão do usuário):** `~/.codex/AGENTS.md` passou de 3,2 KB (cópia das regras do orquestrador) para 1,3 KB só com regras de executor; backup em `AGENTS.md.bak-20261108`. O app do Codex não o sincroniza com o `CLAUDE.md`. O aviso de papel do `codex-agent.ps1` foi removido.
4. **Saída de ferramentas:** logs de Maven/Spring entram no contexto do Codex. O rodapé já usa `mvn -q`; avaliar a opção de limite de saída de ferramenta do Codex (`tool_output_token_limit`, citada em guia de terceiros — confirmar na versão instalada antes de usar).
5. **Uma rodada de revisão por task** e LOW direto para issue; a segunda rodada só para HIGH.
6. **Gemini e Ollama** para checagem de design e rascunhos (não gastam o Codex nem o Claude).
7. **Medição:** CSV por execução (script novo, seção 4).

## 3. Quadro do GitHub

Estado em 08/11: todas as ideias viraram issue (bom), mas:
- issues novas sem `Fase` e `Esforço`;
- só `Todo` e `Done`, nada em `In Progress`;
- issues fechadas com o código só no PC local;
- sem agrupamento por entrega nem ordem de prioridade.

Mudanças:
1. `board.ps1 new` exige `-Fase` e `-Esforco` (P/M/G) e aceita `-Prioridade`.
2. `board.ps1 start -Repo -Issue` → `In Progress`; `board.ps1 close` só depois do push; antes disso, etiqueta `local` e status `Done (local)` ou comentário.
3. Novas fases: `Seleção LCU`, `Motor`, `Dados de elo alto`. Preencher as issues abertas de 07–08/11.
4. **Corpo de issue padrão** (vira contexto pronto para o Codex): Problema (com caso real), Causa conhecida, Decisões do usuário, Critério de pronto, Arquivos prováveis, Fixtures/dados reais, Fora de escopo. A spec do Codex pode nascer da issue sem reescrita.

## 4. Scripts novos

| Script | Para quê |
|---|---|
| `integrity-check.ps1` | depois de queda de luz: `git fsck` nos repos, bytes nulos nos arquivos alterados, `validate` em todas as coleções do Mongo, `stack.ps1 status` e `up` do que caiu |
| `lcu-snapshot.ps1 antes\|depois` | foto das páginas de runas e arsenais pela sonda, restaura as fixtures do commit e compara; alerta qualquer mudança fora da página `live-builds` e dos sets `live-builds-*` |
| `delegate.ps1` + `codex-report.ps1` | uma linha de CSV por execução (data, repo, delegado, modelo, esforço, duração, código de saída, cota esgotada) e relatório por dia/janela |
| `knowledge-check.ps1 -Champion -Position [-Patch]` | mostra a entrada do pacote do processor: runa principal, página, feitiços, caminho de itens, partidas, `lowSample` |

## 5. Contexto para o Codex e outros agentes

1. **AGENTS.md por repo** já aponta scripts e comandos; acrescentar uma seção "Regras de dados do usuário" no live-builds (só a página `live-builds` e sets `live-builds-*`; preservar o JSON original; nunca apagar sem confirmação).
2. **Fixtures reais** (`tests/fixtures/lcu/`, pacote recortado) são o melhor contexto: citar o caminho na spec em vez de descrever o formato.
3. **Glossário** (`docs/lol/glossario.md`) citado nas specs com termos do jogo.
4. **Decisões do usuário** ficam na issue e no design; a spec cita o trecho, não resume.
