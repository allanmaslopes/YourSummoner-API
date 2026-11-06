package com.your.summoner.infrastructure.adapter.in.rest;

import com.your.summoner.application.port.in.GetChampionDetailUseCase;
import com.your.summoner.application.port.in.GetGameVersionUseCase;
import com.your.summoner.application.port.in.ListChampionMasteriesUseCase;
import com.your.summoner.application.port.in.ListChampionSkinsUseCase;
import com.your.summoner.application.port.in.ListLaneChampionsUseCase;
import com.your.summoner.domain.model.Lane;
import com.your.summoner.domain.model.RiotId;
import com.your.summoner.infrastructure.adapter.in.rest.dto.ChampionDetailResponse;
import com.your.summoner.infrastructure.adapter.in.rest.dto.ChampionMasteryResponse;
import com.your.summoner.infrastructure.adapter.in.rest.dto.ChampionSkinResponse;
import com.your.summoner.infrastructure.adapter.in.rest.dto.VersionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/champions", produces = "application/json")
@Tag(name = "Champions", description = "Campeões do Data Dragon e maestrias e lanes dos jogadores na Riot API.")
public class ChampionController {

    private static final String PATCH_DESCRIPTION =
            "Patch do catálogo (ex.: 16.21.1). Padrão: patch ativo. Patch desconhecido responde 404";
    private static final String PLATFORM_DESCRIPTION = "Plataforma da Riot (BR1, NA1, EUW1, KR...). Padrão: BR1";

    private final GetGameVersionUseCase getVersion;
    private final GetChampionDetailUseCase getDetail;
    private final ListChampionSkinsUseCase listSkins;
    private final ListChampionMasteriesUseCase listMasteries;
    private final ListLaneChampionsUseCase listLaneChampions;
    private final PlatformResolver platforms;
    private final RestDtoMapper mapper;

    public ChampionController(GetGameVersionUseCase getVersion, GetChampionDetailUseCase getDetail,
                              ListChampionSkinsUseCase listSkins, ListChampionMasteriesUseCase listMasteries,
                              ListLaneChampionsUseCase listLaneChampions, PlatformResolver platforms,
                              RestDtoMapper mapper) {
        this.getVersion = getVersion;
        this.getDetail = getDetail;
        this.listSkins = listSkins;
        this.listMasteries = listMasteries;
        this.listLaneChampions = listLaneChampions;
        this.platforms = platforms;
        this.mapper = mapper;
    }

    @GetMapping("/findVersion")
    @Operation(summary = "Patch atual do Data Dragon")
    @ApiResponse(responseCode = "200", description = "Versão atual do Data Dragon")
    @ApiResponse(responseCode = "502", description = "Credencial rejeitada ou resposta inválida do upstream")
    @ApiResponse(responseCode = "503", description = "Upstream indisponível ou circuito aberto")
    public VersionResponse version() {
        return new VersionResponse(getVersion.execute());
    }

    @GetMapping("/find-champion/{championName}")
    @Operation(summary = "Ficha do campeão (id do Data Dragon ou nome, sem diferenciar caixa)")
    @ApiResponse(responseCode = "200", description = "Ficha do campeão")
    @ApiResponse(responseCode = "404", description = "Não encontrado")
    @ApiResponse(responseCode = "502", description = "Credencial rejeitada ou resposta inválida do upstream")
    @ApiResponse(responseCode = "503", description = "Upstream indisponível ou circuito aberto")
    public ChampionDetailResponse champion(
            @Parameter(description = "Id do campeão no Data Dragon (ex.: Aatrox) ou nome, sem diferenciar caixa")
            @PathVariable("championName") String championName,
            @Parameter(description = PATCH_DESCRIPTION) @RequestParam(value = "patch", required = false) String patch) {
        return mapper.toResponse(getDetail.execute(championName, patch));
    }

    @GetMapping("/{championName}/skins")
    @Operation(summary = "Skins do campeão no patch, com as URLs de splash e loading")
    @ApiResponse(responseCode = "200", description = "Skins do campeão")
    @ApiResponse(responseCode = "404", description = "Campeão ou patch não encontrado")
    @ApiResponse(responseCode = "502", description = "Credencial rejeitada ou resposta inválida do upstream")
    @ApiResponse(responseCode = "503", description = "Upstream indisponível ou circuito aberto")
    public List<ChampionSkinResponse> skins(
            @Parameter(description = "Id do campeão no Data Dragon (ex.: Aatrox) ou nome, sem diferenciar caixa")
            @PathVariable("championName") String championName,
            @Parameter(description = PATCH_DESCRIPTION) @RequestParam(value = "patch", required = false) String patch) {
        return mapper.toSkinResponses(listSkins.execute(championName, patch));
    }

    @GetMapping("/by-mastery/{nick}/{tag}")
    @Operation(summary = "Todas as maestrias do jogador, por pontos")
    @ApiResponse(responseCode = "200", description = "Maestrias do jogador, ordenadas por pontos")
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @ApiResponse(responseCode = "404", description = "Não encontrado")
    @ApiResponse(responseCode = "429", description = "Rate limit da Riot (header Retry-After)")
    @ApiResponse(responseCode = "502", description = "Credencial rejeitada ou resposta inválida do upstream")
    @ApiResponse(responseCode = "503", description = "Upstream indisponível ou circuito aberto")
    public List<ChampionMasteryResponse> byMastery(
            @Parameter(description = "Nome do invocador (parte antes do #)") @PathVariable("nick") String nick,
            @Parameter(description = "Tag do invocador (parte depois do #)") @PathVariable("tag") String tag,
            @Parameter(description = PLATFORM_DESCRIPTION) @RequestParam(value = "platform", required = false) String platform,
            @Parameter(description = PATCH_DESCRIPTION) @RequestParam(value = "patch", required = false) String patch) {
        return mapper.toMasteryResponses(
                listMasteries.execute(new RiotId(nick, tag), platforms.resolve(platform), patch));
    }

    @GetMapping("/summoner/{nick}/tag/{tag}/lane/{lane}")
    @Operation(summary = "Campeões do jogador jogados na lane, com o pick rate global")
    @ApiResponse(responseCode = "200", description = "Campeões do jogador na lane, com o pick rate global")
    @ApiResponse(responseCode = "400", description = "Entrada inválida")
    @ApiResponse(responseCode = "404", description = "Não encontrado")
    @ApiResponse(responseCode = "429", description = "Rate limit da Riot (header Retry-After)")
    @ApiResponse(responseCode = "502", description = "Credencial rejeitada ou resposta inválida do upstream")
    @ApiResponse(responseCode = "503", description = "Upstream indisponível ou circuito aberto")
    public List<ChampionMasteryResponse> byLane(
            @Parameter(description = "Nome do invocador (parte antes do #)") @PathVariable("nick") String nick,
            @Parameter(description = "Tag do invocador (parte depois do #)") @PathVariable("tag") String tag,
            @Parameter(description = "Lane: TOP, JUNGLE, MIDDLE, BOTTOM ou SUPPORT (aceita apelidos como mid, adc, sup)")
            @PathVariable("lane") @NotBlank String lane,
            @Parameter(description = PLATFORM_DESCRIPTION) @RequestParam(value = "platform", required = false) String platform,
            @Parameter(description = PATCH_DESCRIPTION) @RequestParam(value = "patch", required = false) String patch) {
        return mapper.toMasteryResponses(listLaneChampions.execute(
                new RiotId(nick, tag), platforms.resolve(platform), Lane.from(lane), patch));
    }
}
