# Patches e Versões no League of Legends

## Cadência de Patches
- **Frequência:** A cada 2 semanas, habitualmente às quartas-feiras.
- **Exceções:** Hotfixes de emergência para bugs críticos ou desbalanceamentos graves que quebram o jogo (podem ocorrer fora da quarta-feira).

## Versão do Jogo vs. Versão do Data Dragon vs. Nome Comercial

| Conceito | Formato Exemplo | Descrição |
|---|---|---|
| Versão do Jogo | `14.1` | Identificador major.minor do cliente do jogo. |
| Versão do Data Dragon | `14.1.1` | Identificador patch de versão de dados estáticos publicado no CDN. |
| Nome Comercial | `Patch 14.1` | Nome utilizado nas notas de atualização e comunicação com a comunidade. |

## Ciclo de Publicação no Data Dragon
- O Data Dragon costuma publicar a versão nova em seu CDN algumas horas depois que o patch do jogo entra no ar.
- Hotfixes pequenos no jogo às vezes **não** geram uma versão nova no Data Dragon.
- Por conta dessa dessincronização, a API utiliza o arquivo `versions.json` do Data Dragon como gatilho de atualização, e não o calendário.

## Troca Automática de Patch na API
- **Job de Verificação:** Um job agendado (executado de hora em hora) consulta o `versions.json` do Data Dragon.
- **Ingestão Completa:** Quando um patch novo é detectado, a API realiza a ingestão atômica de todos os dados do patch (`champion.json` e `champion/{id}.json`) e marca o patch como `READY` somente após a conclusão da ingestão.
- **Troca Transparente:** A requisição do usuário nunca recai sobre um patch parcialmente ingerido. Se a ingestão falhar, o patch anterior continua sendo o ativo e o status do novo patch muda para `FAILED`.
- **Patches Antigos Sob Demanda:** É possível consultar dados de patches anteriores enviando o parâmetro opcional `?patch=` nos endpoints suportados. Patches antigos ainda não gravados localmente são ingeridos na hora da consulta.
