package com.your.summoner.infrastructure.adapter.in.rest;

import com.your.summoner.application.port.in.ListPatchesUseCase;
import com.your.summoner.infrastructure.adapter.in.rest.dto.PatchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/patches", produces = "application/json")
@Tag(name = "Patches", description = "Patches do Data Dragon conhecidos pela API e o estado da ingestão de cada um.")
public class PatchController {

    private final ListPatchesUseCase listPatches;
    private final RestDtoMapper mapper;

    public PatchController(ListPatchesUseCase listPatches, RestDtoMapper mapper) {
        this.listPatches = listPatches;
        this.mapper = mapper;
    }

    /** A ordem vem do catálogo, que guarda a de {@code versions.json}: a mais nova primeiro. */
    @GetMapping
    @Operation(summary = "Patches conhecidos, do mais novo para o mais antigo, com o ativo e o status por locale")
    @ApiResponse(responseCode = "200", description = "Patches conhecidos")
    @ApiResponse(responseCode = "503", description = "Armazenamento do catálogo indisponível")
    public List<PatchResponse> patches() {
        return mapper.toPatchResponses(listPatches.execute());
    }
}
