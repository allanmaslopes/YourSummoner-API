package com.your.summoner.application.port.in;

import com.your.summoner.domain.model.Patch;

import java.util.List;

public interface ListPatchesUseCase {

    List<Patch> execute();
}
