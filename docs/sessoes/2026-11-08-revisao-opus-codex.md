# Revisão de arquiteto (Opus) do que o Codex entregou

Data: 2026-11-08. Escopo: código entregue pelo Codex (e Sonnet de reserva) em 07–08/11 no live-builds e no processor, mais os 16 relatórios de revisão linha a linha.

## 1. O que está bom

- **Testes:** 221 testes no live-builds, com fixtures reais (cliente da LCU, pacote real, Graves, item.json). Revisões e `verify` antes de cada commit.
- **Fronteiras de dados respeitadas** depois das correções: só a página `live-builds` e os sets `live-builds-*` são tocados; JSON do cliente preservado; anonimização.
- **Processor enxuto:** `KnowledgeAggregator` com 314 linhas, regras claras e testadas.
- **Modo enxuto + Terra** deu 23 execuções por cota e 22 sucessos.

## 2. O que precisa melhorar no código

| # | Problema | Onde | Efeito | Correção |
|---|---|---|---|---|
| 1 | **Arquivo gigante**: `ui.rs` tem 2.759 linhas e 98 funções misturando desenho, worker/threads, orquestração da LCU (`apply_and_verify*`), carregamento de ícones (HTTP, cache, PNG), autosave, trackers de requisição e configurações. `view.rs` tem 1.508 e `ui_editors.rs` 1.155. | live-builds | Cada tarefa do Codex lê o arquivo inteiro (mais tokens e mais cota), as tarefas de tela não podem rodar em paralelo e os bugs de estado assíncrono nasceram aqui. | Dividir sem mudar comportamento: `ui/worker.rs` (UiWorker, comandos, LCU), `ui/icons.rs` (IconLoader), `ui/selection.rs` (SelectionUi, autosave, trackers), `ui/screens/*.rs` (desenho). |
| 2 | **Estado assíncrono frágil**: aplicação depois da troca de campeão, resposta antiga apagando rascunho, retry nunca religado, `user_edited` sempre falso. Corrigido, mas por remendos de rodada em rodada. | `ui.rs` | Classe de bug que mais voltou nas revisões. | Um único modelo de requisição com chave e geração (`RequestId { key, generation }`) usado por todo comando do worker, com teste genérico de descarte. |
| 3 | **Dados externos com `unwrap`** protegidos por checagem anterior (`from_riot_bin` lê os campos duas vezes). | `lcu/runes.rs` | Funciona hoje; quebra se a checagem e a leitura divergirem. | `find_recommendation` devolve os valores já lidos. |
| 4 | **Regras duplicadas**: "caminho com 3+ itens" repetido 3 vezes no aggregator; tabela de fragmentos e parse de versão em mais de um lugar no live-builds. | processor, live-builds | Correção em um lugar esquece os outros (o HIGH do `buildPaths` veio disso). | Um helper por regra; uma tabela de fragmentos; uma função de versão. |
| 5 | **Testes que não provam** (`is_none_or` com `None`, `.all()` em lista vazia, fixtures inventadas, "TDD" com teste escrito junto da implementação). | vários | Cobertura aparente. | Exigir no prompt a saída do teste vermelho e proibir esses padrões. |

## 3. Padrões de erro do Codex (das 16 revisões)

1. **Perda de dados do usuário** (3 vezes): reserializar o JSON do cliente com um modelo incompleto.
2. **Estado obsoleto em operação assíncrona** (4 vezes): resultado de uma seleção/rascunho anterior aplicado no atual.
3. **Esquecer a regra de produto**: "sempre há recomendação", fallback para a Riot, ARAM/Arena.
4. **Entrega parcial do Terra** quando a especificação tinha duas partes grandes (edição + ícones): fez uma e disse "parcial".
5. **Afirmações erradas sobre a plataforma** pelo revisor (ex.: `fs::rename` no Windows) — falsos HIGH.
6. **Limites e magia** espalhados (sortrank, tamanhos, intervalos) sem constante nomeada.

## 4. Como vou escrever os prompts daqui para frente

Modelo de especificação (vai para `D:\Workspace\Scripts\yoursummoner\templates\task.md` e é usado pelo `issue-to-task.ps1`):

1. **Objetivo** em uma frase e **uma entrega por tarefa** (nunca "faça A e B" grandes juntos; o Terra entrega parcial).
2. **Invariantes que não podem quebrar** (bloco fixo por repo, também no `AGENTS.md`):
   - dados do usuário: só tocar o que é do app; preservar o JSON original; nunca apagar sem confirmação;
   - assíncrono: todo resultado carrega chave e geração; descartar o obsoleto;
   - dados externos: nunca `unwrap`/`expect`; erro vira fallback ou mensagem;
   - produto: sempre há recomendação (elo alto → Riot → aviso), só Summoner's Rift aplica sozinho.
3. **Onde mexer**: arquivos e funções exatos, com assinatura; o que não tocar.
4. **Dados reais**: caminho das fixtures reais a usar; proibido inventar.
5. **Testes**: nomes exatos; colar a linha do teste falhando antes da implementação; padrões proibidos (asserção vacuamente verdadeira, `.all()` sem checar tamanho, fixture inventada).
6. **Constantes nomeadas** para todo limite numérico.
7. **Resposta**: arquivos, testes, saída dos comandos, **o que ficou de fora e por quê**.

Modelo de revisão (`templates/review.md`, usado pelo `review.ps1`/`cycle.ps1`):
- incluir o "Review Focus" da tarefa e os **achados já descartados** (com o motivo) para não repetir;
- exigir evidência para afirmação sobre plataforma/biblioteca ("confirme na documentação ou com teste");
- classificar LOW direto como "issue", sem pedir correção.

## 5. Ações propostas

1. Refatorar `ui.rs` em módulos (tarefa do Codex Terra, sem mudança de comportamento, testes existentes como rede) — reduz tokens de toda tarefa futura de tela.
2. Seção "Invariantes" no `AGENTS.md` do live-builds e do processor.
3. `templates/task.md` e `templates/review.md` nos scripts, ligados ao `issue-to-task.ps1`, `review.ps1` e `cycle.ps1`.
4. Helpers para as regras duplicadas (processor e live-builds) e remover o `unwrap` duplo das runas da Riot.
5. Uma rodada só de LOW (live-builds#16, processor#7) com o Luna.
