package com.your.summoner.infrastructure.adapter.out.riot;

import com.your.summoner.domain.exception.ExternalAuthenticationException;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RiotFeignConfigTest {

    private final RiotFeignConfig config = new RiotFeignConfig();

    @Test
    void shouldFailWithoutCallingTheNetworkWhenTokenIsMissing() {
        var interceptor = config.riotToken(Optional::empty);

        assertThrows(ExternalAuthenticationException.class, () -> interceptor.apply(new RequestTemplate()));
    }

    @Test
    void shouldFailWhenTokenIsBlank() {
        var interceptor = config.riotToken(() -> Optional.of("  "));

        assertThrows(ExternalAuthenticationException.class, () -> interceptor.apply(new RequestTemplate()));
    }

    @Test
    void shouldReadTheCredentialOnEveryRequest() {
        var current = new String[] {"first"};
        var interceptor = config.riotToken(() -> Optional.of(current[0]));
        var first = new RequestTemplate();
        var second = new RequestTemplate();

        interceptor.apply(first);
        current[0] = "second";
        interceptor.apply(second);

        assertEquals("first", first.headers().get("X-Riot-Token").iterator().next());
        assertEquals("second", second.headers().get("X-Riot-Token").iterator().next());
    }
}
