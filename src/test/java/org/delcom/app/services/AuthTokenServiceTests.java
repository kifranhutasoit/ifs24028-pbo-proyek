package org.delcom.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.delcom.app.entities.AuthToken;
import org.delcom.app.repositories.AuthTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class AuthTokenServiceTests {
    @Test
    @DisplayName("Berbagai pengujian AuthToken")
    public void testVariousAuthToken() {
        UUID userId = UUID.randomUUID();
        AuthToken authToken = new AuthToken(userId, "token");

        // Membuat user repository palsu
        AuthTokenRepository authTokenRepository = Mockito.mock(AuthTokenRepository.class);

        // Membuat instance AuthToken dengan repository palsu
        AuthTokenService authTokenService = new AuthTokenService(authTokenRepository);
        assertTrue(authTokenService != null);

        // Menguji createAuthToken
        {
            Mockito.when(authTokenRepository.save(Mockito.any(AuthToken.class))).thenReturn(authToken);

            AuthToken result = authTokenService.createAuthToken(authToken);
            assertTrue(result != null);
            assertEquals(authToken.getUserId(), result.getUserId());
            assertEquals(authToken.getToken(), result.getToken());
        }

        // Menguji findUserToken
        {
            Mockito.when(authTokenRepository.findUserToken(userId, "token")).thenReturn(authToken);

            AuthToken result = authTokenService.findUserToken(userId, "token");
            assertTrue(result != null);
            assertEquals(authToken.getUserId(), result.getUserId());
            assertEquals(authToken.getToken(), result.getToken());
        }

        // Menguji deleteAuthToken
        {
            Mockito.doNothing().when(authTokenRepository).deleteByUserId(userId);

            authTokenService.deleteAuthToken(userId);
            Mockito.verify(authTokenRepository, Mockito.times(1)).deleteByUserId(userId);
        }
    }
}
