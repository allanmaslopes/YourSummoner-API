package com.your.summoner.domain.model;

import com.your.summoner.domain.exception.InvalidInputException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdminModelTest {
    @Test
    void testSecretMetadataLast4Of() {
        assertEquals("1234", SecretMetadata.last4Of("RGAPI-abcd-1234"));
        assertEquals("", SecretMetadata.last4Of("abc"));
        assertEquals("", SecretMetadata.last4Of(null));
    }

    @Test
    void testAppSettingsValid() {
        AppSettings settings = new AppSettings("pt_BR", Platform.BR1);
        assertNotNull(settings);
    }

    @Test
    void testAppSettingsInvalidLocale() {
        assertThrows(InvalidInputException.class, () -> new AppSettings("ptbr", Platform.BR1));
        assertThrows(InvalidInputException.class, () -> new AppSettings("PT_br", Platform.BR1));
        assertThrows(InvalidInputException.class, () -> new AppSettings(null, Platform.BR1));
    }

    @Test
    void testAppSettingsInvalidPlatform() {
        assertThrows(InvalidInputException.class, () -> new AppSettings("pt_BR", null));
    }
}
