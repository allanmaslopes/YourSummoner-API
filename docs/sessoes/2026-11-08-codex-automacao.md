# Codex: estado do trabalho e automação

Data: 2026-11-08. Fonte: `D:\Workspace\Scripts\yoursummoner\logs\delegate-metrics.csv` e relatórios `review-*.out`.

## Como está hoje

| Medida (08/11, até a cota acabar às ~15:50) | Valor |
|---|---|
| Execuções | 23 (15 implementações, 8 leituras: revisões e checagens de design) |
| Modelos | 20 Terra (média 4,5 min), 3 Sol (média 10 min) |
| Sucesso | 22 de 23; 1 cortada pela cota |
| Tempo de Codex | ~105 min implementando, ~15 min lendo |
| Revisões linha a linha | 10 hoje; HIGH em 4, só MEDIUM/LOW em 5, limpa em 1 |
| Comparação | ontem, no Sol, a cota rendia 5–8 execuções; hoje rendeu 23 |

Entregas de hoje pelo Codex: processor#8 e #9, build completa no live-builds, Precisão (#17), layout (#18), painel da partida (#19), scripts, rascunho e design do motor.

O que ainda é manual (e gasta tokens do Claude):
1. Escrever cada especificação a partir da conversa/issue.
2. Encadear: `delegate` → `verify` → `review` → ler relatório → escrever a especificação da correção → `delegate` de novo → `verify` → commit → `board.ps1`.
3. Decidir a escada (Terra → Sol) quando o Terra entrega parcial.
4. Retomar quando a cota acaba (hoje por agendamento manual).

## O que automatizar

| # | Automação | O que faz | Economia |
|---|---|---|---|
| 1 | `cycle.ps1 -Repo -TaskFile [-Issue]` | Roda o ciclo inteiro de um card: `board start` → `delegate` (Terra) → `verify` → `review` (Terra). Se a revisão tiver HIGH/MEDIUM, gera sozinho a especificação da correção a partir do relatório e roda **uma** rodada de correção; depois `verify` de novo. Se o `verify` falhar ou o delegado disser "parcial", repete uma vez com Sol médio. No fim, deixa o diff pronto e um resumo (sem commit). | o Claude só lê o resumo e o diff final e faz o commit; corta 3–5 turnos por card |
| 2 | `issue-to-task.ps1 -Repo -Issue` | Gera o arquivo de tarefa a partir do corpo da issue no modelo padrão (Problema, Causa, Decisões, Pronto quando, Arquivos, Fixtures, Fora de escopo), com o rodapé do repo. | especificação sem reescrita quando a issue já está bem descrita |
| 3 | Cota esgotada → retomada | `delegate.ps1` lê "try again at HH:MM", grava `logs\codex-retry.json` com a tarefa e cria uma tarefa agendada do Windows (`schtasks`) que roda o `cycle.ps1` nessa hora. | nada parado esperando cota; não depende do app do Claude aberto |
| 4 | `codex-queue.ps1` | Fila de cards em `logs\codex-queue.json`, respeitando "um escritor por arquivo/repo" (cards do mesmo repo em sequência, de repos diferentes em paralelo). Pode rodar pela tarefa agendada à noite. Resultado de cada card numa branch local `codex/<issue>`, nunca no `master`; o Claude revisa e integra de manhã. | trabalho noturno sem o Claude ligado |
| 5 | Commit sugerido | O `cycle.ps1` escreve a mensagem de commit proposta (título do card, o que mudou, achados da revisão) em `logs\commit-<issue>.txt`. | menos texto escrito pelo Claude |

Regras que não mudam: o Codex não faz commit nem push; o Claude revisa o diff e faz o commit; push só com pedido do usuário; nada grava no cliente do LoL sem teste do usuário.

## Ordem sugerida

1. `cycle.ps1` (maior ganho).
2. Retomada automática pela cota (item 3).
3. `issue-to-task.ps1`.
4. Fila noturna com branches (`codex-queue.ps1`).
