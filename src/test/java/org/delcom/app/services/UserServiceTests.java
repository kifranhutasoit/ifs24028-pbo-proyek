package org.delcom.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.delcom.app.entities.User;
import org.delcom.app.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class UserServiceTests {
    @Test
    @DisplayName("Berbagai pengujian UserService")
    public void testVariousUserService() {
        User user = new User("Test User", "testuser@example.com", "password123");

        // Membuat user repository palsu
        UserRepository userRepository = Mockito.mock(UserRepository.class);

        // Membuat instance UserService dengan repository palsu
        UserService userService = new UserService(userRepository);
        assertTrue(userService != null);

        // Menguji createUser
        {
            Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

            User result = userService.createUser(user.getName(), user.getEmail(), user.getPassword());
            assertTrue(result != null);
            assertEquals(user.getName(), result.getName());
            assertEquals(user.getEmail(), result.getEmail());
            assertEquals(user.getPassword(), result.getPassword());
        }

        // Menguji getUserByEmail dengan email yang ada
        {
            Mockito.when(userRepository.findFirstByEmail(user.getEmail()))
                    .thenReturn(java.util.Optional.of(user));

            User result = userService.getUserByEmail(user.getEmail());
            assertTrue(result != null);
            assertEquals(user.getEmail(), result.getEmail());
        }

        // Menguji getUserByEmail dengan email yang tidak ada
        {
            Mockito.when(userRepository.findFirstByEmail("notfound@example.com"))
                    .thenReturn(java.util.Optional.empty());

            User result = userService.getUserByEmail("notfound@example.com");
            assertTrue(result == null);
        }

        // Menguji getUserById dengan ID yang ada
        {
            Mockito.when(userRepository.findById(user.getId()))
                    .thenReturn(java.util.Optional.of(user));

            User result = userService.getUserById(user.getId());
            assertTrue(result != null);
            assertEquals(user.getId(), result.getId());
        }

        // Menguji getUserById dengan ID yang tidak ada
        {
            Mockito.when(userRepository.findById(Mockito.any()))
                    .thenReturn(java.util.Optional.empty());

            User result = userService.getUserById(java.util.UUID.randomUUID());
            assertTrue(result == null);
        }

        // Menguji updateUser dengan ID yang ada
        {
            Mockito.when(userRepository.findById(user.getId()))
                    .thenReturn(java.util.Optional.of(user));
            Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

            User result = userService.updateUser(user.getId(), "Updated Name", "updated@example.com");
            assertTrue(result != null);
            assertEquals("Updated Name", result.getName());
            assertEquals("updated@example.com", result.getEmail());
        }

        // Menguji updateUser dengan ID yang tidak ada
        {
            Mockito.when(userRepository.findById(Mockito.any()))
                    .thenReturn(java.util.Optional.empty());
            User result = userService.updateUser(java.util.UUID.randomUUID(), "Name", "email@example.com");
            assertTrue(result == null);
        }

        // Menguji updatePassword dengan ID yang ada
        {
            Mockito.when(userRepository.findById(user.getId()))
                    .thenReturn(java.util.Optional.of(user));
            Mockito.when(userRepository.save(Mockito.any(User.class))).thenReturn(user);

            User result = userService.updatePassword(user.getId(), "newpassword123");
            assertTrue(result != null);
            assertEquals("newpassword123", result.getPassword());
        }

        // Menguji updatePassword dengan ID yang tidak ada
        {
            Mockito.when(userRepository.findById(Mockito.any()))
                    .thenReturn(java.util.Optional.empty());
            User result = userService.updatePassword(java.util.UUID.randomUUID(), "newpassword123");
            assertTrue(result == null);
        }
    }
}
