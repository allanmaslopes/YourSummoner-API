package com.your.summoner.application.service;

import com.your.summoner.application.port.out.AccountProvider;
import com.your.summoner.application.port.out.MatchProvider;
import com.your.summoner.domain.exception.ExternalServiceUnavailableException;
import com.your.summoner.domain.exception.InvalidInputException;
import com.your.summoner.domain.exception.RateLimitExceededException;
import com.your.summoner.domain.exception.ResourceNotFoundException;
import com.your.summoner.domain.model.Account;
import com.your.summoner.domain.model.Match;
import com.your.summoner.domain.model.MatchQuery;
import com.your.summoner.domain.model.Platform;
import com.your.summoner.domain.model.PlayerMatch;
import com.your.summoner.domain.model.Region;
import com.your.summoner.domain.model.RiotId;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MatchServiceTest {

    private final AccountProvider accounts = mock(AccountProvider.class);
    private final MatchProvider matches = mock(MatchProvider.class);
    private final MatchService service = new MatchService(new AccountResolver(accounts), matches);

    @Test
    void listsMatchIdsInTheMatchRegionOfThePlatform() {
        var riotId = new RiotId("Tester", "EUW");
        var query = new MatchQuery(null, null, 420, null, 0, 5);
        when(accounts.findByRiotId(riotId, Region.EUROPE)).thenReturn(Optional.of(new Account("p", "Tester", "EUW")));
        when(matches.findMatchIds("p", Region.EUROPE, query)).thenReturn(List.of("EUW1_1"));

        assertEquals(List.of("EUW1_1"), service.execute(riotId, Platform.EUW1, query));
    }

    @Test
    void matchRegionComesFromTheIdPrefix() {
        when(matches.findById("OC1_9", Region.SEA)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.execute("OC1_9"));
    }

    @Test
    void malformedMatchIdIsRejectedBeforeCallingRiot() {
        assertThrows(InvalidInputException.class, () -> service.execute("123"));
        verifyNoInteractions(matches);
    }

    @Test
    void unknownAccountIsNotFound() {
        when(accounts.findByRiotId(any(), any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.execute(new RiotId("Ghost", "BR1"), Platform.BR1, MatchQuery.empty()));
    }

    // ---- history ----

    private static final RiotId ME = new RiotId("Tester", "BR1");

    private static Match match(String id, String... puuids) {
        var participants = java.util.Arrays.stream(puuids)
                .map(u -> new Match.Participant(u, "n", "t", 100, 1, "Champ-" + u, "TOP", 10, 1, 1, 1, 0, 0, 0, 0, 0,
                        true, List.of(), 4, 11, 0, 0))
                .toList();
        return new Match(id, "BR1", 0L, 0L, "CLASSIC", "MATCHED_GAME", "1", 420, 11, participants, List.of());
    }

    private void givenAccountAndIds(String... ids) {
        when(accounts.findByRiotId(ME, Region.AMERICAS)).thenReturn(Optional.of(new Account("me", "Tester", "BR1")));
        when(matches.findMatchIds(eq("me"), eq(Region.AMERICAS), any())).thenReturn(List.of(ids));
    }

    private static MatchQuery countOf(Integer count) {
        return new MatchQuery(null, null, null, null, null, count);
    }

    @Test
    void historyPreservesIdOrderEvenWhenTheFirstDetailIsTheSlowest() {
        givenAccountAndIds("BR1_1", "BR1_2", "BR1_3");
        var delays = java.util.Map.of("BR1_1", 150L, "BR1_2", 50L, "BR1_3", 0L);
        when(matches.findById(any(), eq(Region.AMERICAS))).thenAnswer(inv -> {
            String id = inv.getArgument(0);
            Thread.sleep(delays.get(id));
            return Optional.of(match(id, "me"));
        });

        List<PlayerMatch> result = service.history(ME, Platform.BR1, countOf(3));

        assertEquals(List.of("BR1_1", "BR1_2", "BR1_3"), result.stream().map(pm -> pm.match().matchId()).toList());
    }

    @Test
    void historySkipsMatchesRiotDoesNotReturn() {
        givenAccountAndIds("BR1_1", "BR1_2", "BR1_3");
        when(matches.findById("BR1_1", Region.AMERICAS)).thenReturn(Optional.of(match("BR1_1", "me")));
        when(matches.findById("BR1_2", Region.AMERICAS)).thenReturn(Optional.empty());
        when(matches.findById("BR1_3", Region.AMERICAS)).thenReturn(Optional.of(match("BR1_3", "me")));

        var result = service.history(ME, Platform.BR1, countOf(3));

        assertEquals(List.of("BR1_1", "BR1_3"), result.stream().map(pm -> pm.match().matchId()).toList());
    }

    @Test
    void historySkipsMatchesWithoutThePlayer() {
        givenAccountAndIds("BR1_1", "BR1_2");
        when(matches.findById("BR1_1", Region.AMERICAS)).thenReturn(Optional.of(match("BR1_1", "someone-else")));
        when(matches.findById("BR1_2", Region.AMERICAS)).thenReturn(Optional.of(match("BR1_2", "other", "me")));

        var result = service.history(ME, Platform.BR1, countOf(2));

        assertEquals(1, result.size());
        assertEquals("BR1_2", result.get(0).match().matchId());
        assertEquals("me", result.get(0).player().puuid());
    }

    @Test
    void historyRethrowsTheSameDomainExceptionWithoutWrapping() {
        givenAccountAndIds("BR1_1", "BR1_2", "BR1_3");
        var limited = new RateLimitExceededException(Duration.ofSeconds(7));
        when(matches.findById("BR1_1", Region.AMERICAS)).thenReturn(Optional.of(match("BR1_1", "me")));
        when(matches.findById("BR1_2", Region.AMERICAS)).thenThrow(limited);
        when(matches.findById("BR1_3", Region.AMERICAS)).thenReturn(Optional.of(match("BR1_3", "me")));

        var thrown = assertThrows(RuntimeException.class, () -> service.history(ME, Platform.BR1, countOf(3)));

        assertSame(limited, thrown);
    }

    @Test
    void historyRejectsCountOutsideOneToHundredBeforeCallingAnyPort() {
        for (Integer bad : new Integer[]{0, 101, null}) {
            assertThrows(InvalidInputException.class, () -> service.history(ME, Platform.BR1, countOf(bad)),
                    "count=" + bad);
        }
        verifyNoInteractions(accounts, matches);
    }

    @Test
    void historyAcceptsTheBoundaryCounts() {
        givenAccountAndIds();

        assertTrue(service.history(ME, Platform.BR1, countOf(1)).isEmpty());
        assertTrue(service.history(ME, Platform.BR1, countOf(100)).isEmpty());
    }

    @Test
    void historyIsEmptyWhenThereAreNoMatchIds() {
        givenAccountAndIds();

        assertTrue(service.history(ME, Platform.BR1, countOf(5)).isEmpty());
        verify(matches, never()).findById(any(), any());
    }

    @Test
    void historyNeverRunsMoreThanFourDetailRequestsAtOnce() {
        var ids = java.util.stream.IntStream.rangeClosed(1, 10).mapToObj(i -> "BR1_" + i).toArray(String[]::new);
        givenAccountAndIds(ids);
        var running = new AtomicInteger();
        var max = new AtomicInteger();
        when(matches.findById(any(), eq(Region.AMERICAS))).thenAnswer(inv -> {
            max.accumulateAndGet(running.incrementAndGet(), Math::max);
            try {
                Thread.sleep(40);
                return Optional.of(match(inv.getArgument(0), "me"));
            } finally {
                running.decrementAndGet();
            }
        });

        var result = service.history(ME, Platform.BR1, countOf(10));

        assertEquals(10, result.size());
        assertTrue(max.get() <= 4, "max concurrent was " + max.get());
        assertTrue(max.get() > 1, "expected some parallelism, max was " + max.get());
    }

    @Test
    void historyRunsAtMostFourCallsAtOnce() {
        var ids = java.util.stream.IntStream.rangeClosed(1, 20).mapToObj(i -> "BR1_" + i).toList();
        var running = new AtomicInteger();
        var peak = new AtomicInteger();
        MatchProvider fake = new MatchProvider() {
            @Override
            public List<String> findMatchIds(String puuid, Region region, MatchQuery query) {
                return ids;
            }

            @Override
            public Optional<Match> findById(String matchId, Region region) {
                peak.accumulateAndGet(running.incrementAndGet(), Math::max);
                try {
                    Thread.sleep(30);
                    return Optional.of(match(matchId, "me"));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                } finally {
                    running.decrementAndGet();
                }
            }
        };
        when(accounts.findByRiotId(ME, Region.AMERICAS)).thenReturn(Optional.of(new Account("me", "Tester", "BR1")));

        var result = new MatchService(new AccountResolver(accounts), fake).history(ME, Platform.BR1, countOf(20));

        assertEquals(20, result.size());
        assertTrue(peak.get() <= 4, "peak was " + peak.get());
        assertTrue(peak.get() > 1, "expected parallelism, peak was " + peak.get());
    }

    @Test
    void historyFailsWhenRiotIsUnavailable() {
        givenAccountAndIds("BR1_1", "BR1_2");
        var down = new ExternalServiceUnavailableException("riot", "503");
        when(matches.findById("BR1_1", Region.AMERICAS)).thenThrow(down);
        when(matches.findById("BR1_2", Region.AMERICAS)).thenReturn(Optional.of(match("BR1_2", "me")));

        assertSame(down, assertThrows(RuntimeException.class, () -> service.history(ME, Platform.BR1, countOf(2))));
    }
}
