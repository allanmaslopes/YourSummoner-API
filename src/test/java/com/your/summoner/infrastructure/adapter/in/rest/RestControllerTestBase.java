package com.your.summoner.infrastructure.adapter.in.rest;

import com.your.summoner.application.port.in.GetChampionDetailUseCase;
import com.your.summoner.application.port.in.GetGameVersionUseCase;
import com.your.summoner.application.port.in.GetMatchHistoryUseCase;
import com.your.summoner.application.port.in.GetMatchUseCase;
import com.your.summoner.application.port.in.GetSummonerProfileUseCase;
import com.your.summoner.application.port.in.ListChampionMasteriesUseCase;
import com.your.summoner.application.port.in.ListChampionSkinsUseCase;
import com.your.summoner.application.port.in.ListLaneChampionsUseCase;
import com.your.summoner.application.port.in.ListMatchIdsUseCase;
import com.your.summoner.application.port.out.StaticDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Contexto único para os testes dos controllers: aplicação inteira (perfil {@code test}, Mongo embarcado, job de
 * patch desligado) com os casos de uso mockados.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class RestControllerTestBase {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    GetGameVersionUseCase getVersion;
    @MockitoBean
    GetChampionDetailUseCase getDetail;
    @MockitoBean
    ListChampionSkinsUseCase listSkins;
    @MockitoBean
    ListChampionMasteriesUseCase listMasteries;
    @MockitoBean
    ListLaneChampionsUseCase listLaneChampions;
    @MockitoBean
    GetSummonerProfileUseCase getProfile;
    @MockitoBean
    GetMatchUseCase getMatch;
    @MockitoBean
    GetMatchHistoryUseCase getHistory;
    @MockitoBean
    ListMatchIdsUseCase listMatchIds;
    /** PatchService também é SyncPatchesUseCase (usado pelo catálogo): mocka-se a porta de saída, não o caso de uso. */
    @MockitoBean
    StaticDataRepository repository;
}
