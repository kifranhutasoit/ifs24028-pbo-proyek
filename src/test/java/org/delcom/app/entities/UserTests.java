package org.delcom.app.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserTests {
    @Test
    @DisplayName("Memembuat instance dari kelas User")
    void testMembuatInstanceUser() throws Exception {
        // User dengan nama, email dan password
        {
            User user = new User("Name", "email@example.com", "password123");

            assertEquals("Name", user.getName());
            assertEquals("email@example.com", user.getEmail());
            assertEquals("password123", user.getPassword());
        }

        // User dengan email dan password
        {
            User user = new User("email@example.com", "password123");
            assertEquals("", user.getName());
            assertEquals("email@example.com", user.getEmail());
            assertEquals("password123", user.getPassword());
        }

        // User dengan nilai default
        {
            User user = new User();

            assertEquals(null, user.getId());
            assertEquals(null, user.getName());
            assertEquals(null, user.getEmail());
            assertEquals(null, user.getPassword());
        }

        // User dengan setNilai
        {
            User user = new User();
            UUID generatedId = UUID.randomUUID();
            user.setId(generatedId);
            user.setName("Set Name");
            user.setEmail("Set Email");
            user.setPassword("Set Password");
            user.onCreate();
            user.onUpdate();

            assertEquals(user.getId(), generatedId);
            assertEquals(user.getName(), "Set Name");
            assertEquals(user.getEmail(), "Set Email");
            assertEquals(user.getPassword(), "Set Password");
            assertTrue(user.getCreatedAt() != null);
            assertTrue(user.getUpdatedAt() != null);
        }
    }
}