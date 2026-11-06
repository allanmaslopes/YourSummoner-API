package com.your.summoner.infrastructure.adapter.in.rest;

import com.your.summoner.application.port.out.SettingsStore;
import com.your.summoner.domain.model.AppSettings;
import com.your.summoner.domain.model.Platform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlatformResolverTest {

    private final SettingsStore settings = mock(SettingsStore.class);
    private final PlatformResolver resolver = new PlatformResolver(settings);

    @Test
    void usesDefaultFromSettingsStore() {
        when(settings.get()).thenReturn(new AppSettings("pt_BR", Platform.NA1));

        assertEquals(Platform.NA1, resolver.resolve(null));
        assertEquals(Platform.NA1, resolver.resolve(" "));
    }

    @Test
    void changeIsVisibleWithoutRestart() {
        when(settings.get()).thenReturn(new AppSettings("pt_BR", Platform.BR1),
                new AppSettings("pt_BR", Platform.EUW1));

        assertEquals(Platform.BR1, resolver.resolve(null));
        assertEquals(Platform.EUW1, resolver.resolve(null));
    }

    @Test
    void explicitParameterWins() {
        when(settings.get()).thenReturn(new AppSettings("pt_BR", Platform.NA1));

        assertEquals(Platform.EUW1, resolver.resolve("EUW1"));
    }
}
