package com.your.summoner.infrastructure.adapter.in.rest;

import com.your.summoner.application.port.in.GetSummonerProfileUseCase;
import com.your.summoner.domain.model.RiotId;
import com.your.summoner.infrastructure.adapter.in.rest.dto.SummonerProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/summoners", produces = "application/json")
@Tag(name = "Summoners", description = "Perfil público dos invocadores da Riot Games API.")
public class SummonerController {

    private final GetSummonerProfileUseCase getProfile;
    private final PlatformResolver platforms;
    private final RestDtoMapper mapper;

    public SummonerController(GetSummonerProfileUseCase getProfile, PlatformResolver platforms, RestDtoMapper mapper) {
        this.getProfile = getProfile;
        this.platforms = platforms;
        this.mapper = mapper;
    }

    @GetMapping("/{nick}/{tag}")
    @Operation(summary = "Perfil do invocador: nível, ícone, ranqueadas e 3 campeões principais")
    @ApiResponse(responseCode = "200", description = "Perfil do invocador")
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @ApiResponse(responseCode = "404", description = "Não encontrado")
    @ApiResponse(responseCode = "429", description = "Rate limit da Riot (header Retry-After)")
    @ApiResponse(responseCode = "502", description = "Credencial rejeitada ou resposta inválida do upstream")
    @ApiResponse(responseCode = "503", description = "Upstream indisponível ou circuito aberto")
    public SummonerProfileResponse profile(
            @Parameter(description = "Nome do invocador (parte antes do #)") @PathVariable("nick") String nick,
            @Parameter(description = "Tag do invocador (parte depois do #)") @PathVariable("tag") String tag,
            @Parameter(description = "Plataforma da Riot (BR1, NA1, EUW1, KR...). Padrão: BR1")
            @RequestParam(value = "platform", required = false) String platform) {
        return mapper.toResponse(getProfile.execute(new RiotId(nick, tag), platforms.resolve(platform)));
    }
}
