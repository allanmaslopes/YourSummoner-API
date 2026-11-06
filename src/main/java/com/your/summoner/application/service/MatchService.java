package com.your.summoner.application.service;

import com.your.summoner.application.port.in.GetMatchHistoryUseCase;
import com.your.summoner.application.port.in.GetMatchUseCase;
import com.your.summoner.application.port.in.ListMatchIdsUseCase;
import com.your.summoner.application.port.out.MatchProvider;
import com.your.summoner.domain.exception.InvalidInputException;
import com.your.summoner.domain.exception.ResourceNotFoundException;
import com.your.summoner.domain.model.Account;
import com.your.summoner.domain.model.Match;
import com.your.summoner.domain.model.MatchQuery;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.PlayerMatch;
import com.your.summoner.domain.model.Region;
import com.your.summoner.domain.model.RiotId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

@Service
public class MatchService implements ListMatchIdsUseCase, GetMatchUseCase, GetMatchHistoryUseCase {

    // A dev key da Riot permite 20 req/s; 4 chamadas simultâneas deixam folga para as demais rotas.
    private static final int HISTORY_CONCURRENCY = 4;
    private static final int HISTORY_MAX = 100;

    private final AccountResolver accountResolver;
    private final MatchProvider matches;

    public MatchService(AccountResolver accountResolver, MatchProvider matches) {
        this.accountResolver = accountResolver;
        this.matches = matches;
    }

    @Override
    public List<String> execute(RiotId riotId, Platform platform, MatchQuery query) {
        Account account = accountResolver.resolve(riotId, platform);
        return matches.findMatchIds(account.puuid(), platform.matchRegion(), query);
    }

    @Override
    public Match execute(String matchId) {
        Platform platform = Platform.fromMatchId(matchId);
        return matches.findById(matchId.trim(), platform.matchRegion())
                .orElseThrow(() -> new ResourceNotFoundException("Match", matchId));
    }

    @Override
    public List<PlayerMatch> history(RiotId riotId, Platform platform, MatchQuery query) {
        Integer count = query.count();
        if (count == null || count < 1 || count > HISTORY_MAX) {
            throw new InvalidInputException("quantity must be between 1 and " + HISTORY_MAX);
        }
        Account account = accountResolver.resolve(riotId, platform);
        Region region = platform.matchRegion();
        List<String> ids = matches.findMatchIds(account.puuid(), region, query);

        var gate = new Semaphore(HISTORY_CONCURRENCY);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Optional<PlayerMatch>>> futures = new ArrayList<>(ids.size());
            for (String id : ids) {
                futures.add(executor.submit(() -> {
                    gate.acquire();
                    try {
                        return matches.findById(id, region)
                                .flatMap(m -> m.participant(account.puuid()).map(p -> new PlayerMatch(m, p)));
                    } finally {
                        gate.release();
                    }
                }));
            }
            // Coleta na ordem dos ids; na primeira falha cancela o resto e propaga a exceção original.
            List<PlayerMatch> history = new ArrayList<>(ids.size());
            try {
                for (Future<Optional<PlayerMatch>> f : futures) {
                    f.get().ifPresent(history::add);
                }
            } catch (ExecutionException e) {
                futures.forEach(f -> f.cancel(true));
                throw e.getCause() instanceof RuntimeException re ? re : new IllegalStateException(e.getCause());
            } catch (InterruptedException e) {
                futures.forEach(f -> f.cancel(true));
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
            return history;
        }
    }
}
