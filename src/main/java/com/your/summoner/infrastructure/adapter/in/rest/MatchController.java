package com.your.summoner.infrastructure.adapter.in.rest;

import com.your.summoner.application.port.in.GetMatchHistoryUseCase;
import com.your.summoner.application.port.in.GetMatchUseCase;
import com.your.summoner.application.port.in.ListMatchIdsUseCase;
import com.your.summoner.domain.model.MatchQuery;
import com.your.summoner.domain.model.RiotId;
import com.your.summoner.infrastructure.adapter.in.rest.dto.MatchHistoryEntryResponse;
import com.your.summoner.infrastructure.adapter.in.rest.dto.MatchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/matchs", produces = "application/json")
@Tag(name = "Matches", description = "Partidas do jogador: ids, histórico com performance e detalhe da partida.")
public class MatchController {

    private static final int DEFAULT_HISTORY = 20;
    private static final String PLATFORM_DESCRIPTION = "Plataforma da Riot (BR1, NA1, EUW1, KR...). Padrão: BR1";

    private final ListMatchIdsUseCase listMatchIds;
    private final GetMatchUseCase getMatch;
    private final GetMatchHistoryUseCase getHistory;
    private final PlatformResolver platforms;
    private final RestDtoMapper mapper;

    public MatchController(ListMatchIdsUseCase listMatchIds, GetMatchUseCase getMatch,
                           GetMatchHistoryUseCase getHistory, PlatformResolver platforms, RestDtoMapper mapper) {
        this.listMatchIds = listMatchIds;
        this.getMatch = getMatch;
        this.getHistory = getHistory;
        this.platforms = platforms;
        this.mapper = mapper;
    }

    @GetMapping("/{nick}/{tag}")
    @Operation(summary = "Ids das partidas do jogador (match-v5)")
    @ApiResponse(responseCode = "200", description = "Ids das partidas do jogador")
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @ApiResponse(responseCode = "404", description = "Não encontrado")
    @ApiResponse(responseCode = "429", description = "Rate limit da Riot (header Retry-After)")
    @ApiResponse(responseCode = "502", description = "Credencial rejeitada ou resposta inválida do upstream")
    @ApiResponse(responseCode = "503", description = "Upstream indisponível ou circuito aberto")
    public List<String> matchIds(
            @Parameter(description = "Nome do invocador (parte antes do #)") @PathVariable("nick") String nick,
            @Parameter(description = "Tag do invocador (parte depois do #)") @PathVariable("tag") String tag,
            @Parameter(description = "Quantidade de ids (0 a 100)")
            @RequestParam(value = "quantity", required = false) @Min(0) @Max(100) Integer quantity,
            @Parameter(description = "Início do intervalo (timestamp Unix em segundos)")
            @RequestParam(value = "startTime", required = false) Long startTime,
            @Parameter(description = "Fim do intervalo (timestamp Unix em segundos)")
            @RequestParam(value = "endTime", required = false) Long endTime,
            @Parameter(description = "Id da fila (queueId da Riot)")
            @RequestParam(value = "queue", required = false) Integer queue,
            @Parameter(description = "Tipo da partida: ranked, normal, tourney ou tutorial")
            @RequestParam(value = "type", required = false) String type,
            @Parameter(description = "Índice do primeiro resultado (0 ou maior)")
            @RequestParam(value = "start", required = false) @Min(0) Integer start,
            @Parameter(description = PLATFORM_DESCRIPTION)
            @RequestParam(value = "platform", required = false) String platform) {
        var query = new MatchQuery(startTime, endTime, queue, type, start, quantity);
        return listMatchIds.execute(new RiotId(nick, tag), platforms.resolve(platform), query);
    }

    @GetMapping("/{nick}/{tag}/history")
    @Operation(summary = "Últimas partidas do jogador com o resumo da performance dele (1 a 100, padrão 20)")
    @ApiResponse(responseCode = "200", description = "Últimas partidas do jogador com o resumo de performance")
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @ApiResponse(responseCode = "404", description = "Não encontrado")
    @ApiResponse(responseCode = "429", description = "Rate limit da Riot (header Retry-After)")
    @ApiResponse(responseCode = "502", description = "Credencial rejeitada ou resposta inválida do upstream")
    @ApiResponse(responseCode = "503", description = "Upstream indisponível ou circuito aberto")
    public List<MatchHistoryEntryResponse> history(
            @Parameter(description = "Nome do invocador (parte antes do #)") @PathVariable("nick") String nick,
            @Parameter(description = "Tag do invocador (parte depois do #)") @PathVariable("tag") String tag,
            @Parameter(description = "Quantidade de partidas (1 a 100). Padrão: 20")
            @RequestParam(value = "quantity", required = false) @Min(1) @Max(100) Integer quantity,
            @Parameter(description = "Id da fila (queueId da Riot)")
            @RequestParam(value = "queue", required = false) Integer queue,
            @Parameter(description = "Tipo da partida: ranked, normal, tourney ou tutorial")
            @RequestParam(value = "type", required = false) String type,
            @Parameter(description = "Índice do primeiro resultado (0 ou maior)")
            @RequestParam(value = "start", required = false) @Min(0) Integer start,
            @Parameter(description = PLATFORM_DESCRIPTION)
            @RequestParam(value = "platform", required = false) String platform) {
        var query = new MatchQuery(null, null, queue, type, start, quantity == null ? DEFAULT_HISTORY : quantity);
        return mapper.toHistoryResponses(getHistory.history(new RiotId(nick, tag), platforms.resolve(platform), query));
    }

    @GetMapping("/matchId/{matchId}")
    @Operation(summary = "Detalhe da partida; a região vem do prefixo do id (BR1_..., KR_...)")
    @ApiResponse(responseCode = "200", description = "Detalhe da partida")
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @ApiResponse(responseCode = "404", description = "Não encontrado")
    @ApiResponse(responseCode = "429", description = "Rate limit da Riot (header Retry-After)")
    @ApiResponse(responseCode = "502", description = "Credencial rejeitada ou resposta inválida do upstream")
    @ApiResponse(responseCode = "503", description = "Upstream indisponível ou circuito aberto")
    public MatchResponse match(
            @Parameter(description = "Id da partida no formato PLATAFORMA_NUMERO (ex.: BR1_1234567890)")
            @PathVariable("matchId") String matchId) {
        return mapper.toResponse(getMatch.execute(matchId));
    }
}
