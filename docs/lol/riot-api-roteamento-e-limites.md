# Riot API: Roteamento e Limites

## Plataformas vs. Regiões de Roteamento
A Riot Games divide os endpoints em dois tipos de escopo de host (`{host}.api.riotgames.com`):

1. **Plataforma (Servidores Locais):** Utilizado para invocador (`summoner-v4`), ranque (`league-v4`) e maestria (`champion-mastery-v4`) do servidor correspondente. Conta (`account-v1`) e partidas (`match-v5`) não usam a plataforma, e sim a região de roteamento.
   - Exemplos: `br1`, `na1`, `euw1`, `kr`, `jp1`, `oc1`, etc.
   - Exemplo de mapeamento: `br1` utiliza a região de roteamento `americas`.

2. **Região de Roteamento (Clusters Globais):** Utilizado para contas globais (`account-v1`) e histórico/detalhes de partidas (`match-v5`).
   - Valores possíveis: `americas`, `europe`, `asia`, `sea`.
   - *Nota:* Contas na região SEA (ex: `oc1`, `sg2`, `vn2`) utilizam o cluster `asia` para o serviço `account-v1`.

## Autenticação
- Toda requisição aos endpoints da Riot exige o header HTTP:
  `X-Riot-Token: <sua-chave>`
- A chave de desenvolvimento (Developer Key) possui validade de **24 horas**, exigindo renovação diária no Portal do Desenvolvedor da Riot.

## Rate Limits (Chave de Desenvolvimento)
- **Limites rígidos:** 20 requisições por segundo e 100 requisições a cada 2 minutos.
- **Tratamento de 429 (Too Many Requests):** A API da Riot retorna o header `Retry-After`. Na API YourSummoner, o rate limit é mitigado pelo uso agressivo de caches em memória (Caffeine), repassando o erro com `Retry-After` caso ocorra esgotamento de cota.

## Riot ID
- Identificação unificada de invocadores no formato `gameName#tagLine` (ex: `Faker#KR1`).
- A busca e o armazenamento em cache tratam `gameName` e `tagLine` em **caixa baixa (lowercase)** para garantir idempotência e evitar duplicidade por diferenciação de maiúsculas/minúsculas.
