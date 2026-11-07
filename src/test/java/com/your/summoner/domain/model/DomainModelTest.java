package com.your.summoner.domain.model;

import com.your.summoner.domain.exception.InvalidInputException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainModelTest {

    @Nested
    class RiotIdTest {

        @Test
        void trimsAndDropsTheHashFromTheTag() {
            RiotId id = new RiotId("  Hide on bush ", "#KR1");
            assertEquals("Hide on bush", id.gameName());
            assertEquals("KR1", id.tagLine());
            assertEquals("Hide on bush#KR1", id.toString());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void rejectsBlankNickname(String nick) {
            assertThrows(InvalidInputException.class, () -> new RiotId(nick, "BR1"));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "#"})
        void rejectsBlankTag(String tag) {
            assertThrows(InvalidInputException.class, () -> new RiotId("Tester", tag));
        }
    }

    @Nested
    class PlatformTest {

        @ParameterizedTest
        @CsvSource({"br1,BR1,AMERICAS,AMERICAS", "KR,KR,ASIA,ASIA", "euw1,EUW1,EUROPE,EUROPE", "vn2,VN2,SEA,ASIA"})
        void routesToTheRightRegions(String raw, Platform platform, Region match, Region account) {
            Platform parsed = Platform.from(raw);
            assertEquals(platform, parsed);
            assertEquals(match, parsed.matchRegion());
            assertEquals(account, parsed.accountRegion());
        }

        @Test
        void rejectsUnknownPlatform() {
            assertThrows(InvalidInputException.class, () -> Platform.from("XX9"));
        }

        @Test
        void readsThePlatformFromTheMatchId() {
            assertEquals(Platform.BR1, Platform.fromMatchId("BR1_3012345678"));
            assertEquals(Platform.KR, Platform.fromMatchId("KR_1"));
            assertThrows(InvalidInputException.class, () -> Platform.fromMatchId("3012345678"));
            assertThrows(InvalidInputException.class, () -> Platform.fromMatchId("_1"));
        }

        @org.junit.jupiter.params.ParameterizedTest
        @org.junit.jupiter.params.provider.ValueSource(strings = {"BR1_abc", "BR1_", "BR1_12x", "BR1-123", "BR1_123_4", "XX1_123"})
        void rejectsMalformedMatchIdsAndUnknownPlatforms(String matchId) {
            assertThrows(InvalidInputException.class, () -> Platform.fromMatchId(matchId));
        }

        @Test
        void trimsTheMatchId() {
            assertEquals(Platform.BR1, Platform.fromMatchId(" BR1_123 "));
        }

        @Test
        void exposesTheHostSubdomain() {
            assertEquals("br1", Platform.BR1.host());
            assertEquals("americas", Region.AMERICAS.host());
        }
    }

    @Nested
    class LaneTest {

        @ParameterizedTest
        @CsvSource({"top,TOP", "jg,JUNGLE", "Mid,MIDDLE", "adc,BOTTOM", "bot,BOTTOM", "sup,SUPPORT", "utility,SUPPORT"})
        void acceptsAliases(String raw, Lane expected) {
            assertEquals(expected, Lane.from(raw));
        }

        @Test
        void rejectsUnknownLane() {
            assertThrows(InvalidInputException.class, () -> Lane.from("river"));
        }
    }

    @Nested
    class LeagueEntryTest {

        @Test
        void computesTotalsAndWinRate() {
            var entry = new LeagueEntry("RANKED_SOLO_5x5", "CHALLENGER", "I", 2256, 448, 371, false);
            assertEquals(819, entry.totalMatches());
            assertEquals(55.0, entry.winRate());
            assertEquals("Challenger", entry.displayTier());
        }

        @Test
        void winRateIsZeroWithoutMatches() {
            var entry = new LeagueEntry("RANKED_FLEX_SR", "", "", 0, 0, 0, false);
            assertEquals(0.0, entry.winRate());
            assertEquals("", entry.displayTier());
        }
    }

    @Nested
    class MatchQueryTest {

        @Test
        void normalizesType() {
            assertEquals("ranked", new MatchQuery(null, null, null, " RANKED ", null, null).type());
        }

        @Test
        void validatesRanges() {
            assertThrows(InvalidInputException.class, () -> new MatchQuery(null, null, null, null, null, 101));
            assertThrows(InvalidInputException.class, () -> new MatchQuery(null, null, null, null, -1, null));
            assertThrows(InvalidInputException.class, () -> new MatchQuery(10L, 5L, null, null, null, null));
            assertThrows(InvalidInputException.class, () -> new MatchQuery(null, null, null, "arena", null, null));
        }
    }

    @Nested
    class ParticipantTest {

        @Test
        void computesKdaAvoidingDivisionByZero() {
            assertEquals(6.0, participant(8, 3, 10).kda());
            assertEquals(3.0, participant(2, 0, 1).kda());
            assertEquals(2.33, participant(3, 3, 4).kda());
        }

        private Match.Participant participant(int kills, int deaths, int assists) {
            return new Match.Participant("p", "n", "t", 100, 1, "c", "TOP", 18, kills, deaths, assists,
                    0, 0, 0, 0, 0, true, List.of(), 4, 14, 0, 0);
        }
    }
}
