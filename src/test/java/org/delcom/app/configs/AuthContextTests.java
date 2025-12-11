package org.delcom.app.configs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.delcom.app.entities.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AuthContextTests {
    @Test
    @DisplayName("Membuat instance kelas AuthContext dengan benar")
    void testMembuatInstanceKelasAuthContextDenganBenar() {
        AuthContext authContext = new AuthContext();

        // Menguji dengan data user tersedia
        {
            User user = new User("Abdullah Ubaid", "test@example.com", "123456");
            authContext.setAuthUser(user);

            assertEquals(user, authContext.getAuthUser());
            assertTrue(authContext.isAuthenticated());
        }

        // Menguji dengan data user kosong
        {
            authContext.setAuthUser(null);
            assertTrue(!authContext.isAuthenticated());
        }

    }
}
