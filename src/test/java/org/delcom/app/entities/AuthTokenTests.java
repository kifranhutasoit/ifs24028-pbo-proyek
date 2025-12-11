package org.delcom.app.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AuthTokenTests {
    @Test
    @DisplayName("Memembuat instance dari kelas AuthToken")
    void testMembuatInstanceAuthToken() throws Exception {
        // AuthToken dengan userId dan token
        {
            AuthToken authToken = new AuthToken(UUID.randomUUID(), "token123");

            assertEquals("token123", authToken.getToken());
            assertTrue(authToken.getUserId() != null);
        }

        // AuthToken dengan nilai default
        {
            AuthToken authToken = new AuthToken();

            assertEquals(null, authToken.getId());
            assertEquals(null, authToken.getToken());
            assertEquals(null, authToken.getUserId());
        }

        // AuthToken dengan setNilai
        {
            AuthToken authToken = new AuthToken();
            UUID generatedId = UUID.randomUUID();
            UUID generatedUserId = UUID.randomUUID();
            authToken.setId(generatedId);
            authToken.setUserId(generatedUserId);
            authToken.setToken("Set Token");
            authToken.onCreate();

            assertEquals(authToken.getId(), generatedId);
            assertEquals(authToken.getUserId(), generatedUserId);
            assertEquals(authToken.getToken(), "Set Token");
            assertTrue(authToken.getCreatedAt() != null);
        }
    }
}