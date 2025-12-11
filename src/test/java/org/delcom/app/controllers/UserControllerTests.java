package org.delcom.app.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.delcom.app.configs.ApiResponse;
import org.delcom.app.configs.AuthContext;
import org.delcom.app.entities.AuthToken;
import org.delcom.app.entities.User;
import org.delcom.app.services.AuthTokenService;
import org.delcom.app.services.UserService;
import org.delcom.app.utils.JwtUtil;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserControllerTests {
    @Test
    @DisplayName("Pengujian UserController dengan berbagai skenario")
    public void testVariousUserController() {

        // Mock AuthService
        AuthTokenService authTokenService = Mockito.mock(AuthTokenService.class);

        // Mock UserService
        UserService userService = Mockito.mock(UserService.class);

        UserController userController = new UserController(userService, authTokenService);
        userController.authContext = new AuthContext();

        // Menguji method registerUser
        {
            // Data tidak valid
            {
                List<User> invalidUsers = List.of(
                        // Nama Null
                        new User(null, "email@example.com", "password123"),
                        // Nama Kosong
                        new User("", "email@example.com", "password123"),
                        // Email Null
                        new User("User", null, "password123"),
                        // Email Kosong
                        new User("User", "", "password123"),
                        // Password Null
                        new User("User", "email@example.com", null),
                        // Password Kosong
                        new User("User", "email@example.com", ""));

                ResponseEntity<ApiResponse<Map<String, UUID>>> result;
                for (User user : invalidUsers) {
                    result = userController.registerUser(user);
                    assert (result != null);
                    assert (result.getStatusCode().is4xxClientError());
                    assert (result.getBody().getStatus().equals("fail"));
                }
            }

            // Email sudah terdaftar
            {
                User existingUser = new User("Existing User", "existing@example.com", "password123");
                Mockito.when(userService.getUserByEmail("existing@example.com")).thenReturn(existingUser);

                ResponseEntity<ApiResponse<Map<String, UUID>>> result = userController.registerUser(existingUser);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            // Registrasi sukses
            {
                User newUser = new User("New User", "new@example.com", "password123");
                newUser.setId(UUID.randomUUID());

                Mockito.when(userService.getUserByEmail("new@example.com")).thenReturn(null);
                Mockito.when(userService.createUser(Mockito.any(String.class), Mockito.any(String.class),
                        Mockito.any(String.class)))
                        .thenReturn(newUser);

                ResponseEntity<ApiResponse<Map<String, UUID>>> result = userController.registerUser(newUser);
                assert (result != null);
                assert (result.getStatusCode().is2xxSuccessful());
                assert (result.getBody().getStatus().equals("success"));
            }
        }

        // Menguji method loginUser
        {
            // Data tidak valid
            {
                List<User> invalidUsers = List.of(
                        // Email Null
                        new User(null, "password123"),
                        // Email Kosong
                        new User("", "password123"),
                        // Password Null
                        new User("user@example.com", null),
                        // Password Kosong
                        new User("user@example.com", ""));

                ResponseEntity<ApiResponse<Map<String, String>>> result;
                for (User user : invalidUsers) {
                    result = userController.loginUser(user);
                    assert (result != null);
                    assert (result.getStatusCode().is4xxClientError());
                    assert (result.getBody().getStatus().equals("fail"));
                }
            }

            // Email atau password salah
            {
                String password = "password123";
                String hashedPassword = new BCryptPasswordEncoder()
                        .encode(password);
                UUID userId = UUID.randomUUID();

                User fakeUser = new User("Fake User", "user@example.com", hashedPassword);
                fakeUser.setId(userId);

                // User tidak ditemuka  n
                Mockito.when(userService.getUserByEmail("user@example.com")).thenReturn(null);

                ResponseEntity<ApiResponse<Map<String, String>>> result = userController
                        .loginUser(fakeUser);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));

                // Password salah
                Mockito.when(userService.getUserByEmail("user@example.com")).thenReturn(fakeUser);
                ResponseEntity<ApiResponse<Map<String, String>>> result2 = userController
                        .loginUser(new User("user@example.com", "wrongpassword"));
                assert (result2 != null);
                assert (result2.getStatusCode().is4xxClientError());
                assert (result2.getBody().getStatus().equals("fail"));
            }

            // Gagagal membuat auth token
            {
                String password = "password123";
                String hashedPassword = new BCryptPasswordEncoder()
                        .encode(password);
                UUID userId = UUID.randomUUID();

                String bearerToken = JwtUtil.generateToken(userId);
                AuthToken fakeAuthToken = new AuthToken(userId, bearerToken);

                User fakeReqUser = new User("Fake User", "user@example.com", password);
                fakeReqUser.setId(userId);

                User fakeUser = new User("Fake User", "user@example.com", hashedPassword);
                fakeUser.setId(userId);

                Mockito.when(userService.getUserByEmail("user@example.com")).thenReturn(fakeUser);

                // Auth token gagal disimpan dengan terdapat token lama
                {
                    // Hapus token lama jika ada
                    Mockito.when(authTokenService.findUserToken(Mockito.any(UUID.class), Mockito.anyString()))
                            .thenReturn(fakeAuthToken);
                    Mockito.doNothing().when(authTokenService).deleteAuthToken(Mockito.any(UUID.class));

                    Mockito.when(authTokenService.createAuthToken(Mockito.any(AuthToken.class))).thenReturn(null);

                    ResponseEntity<ApiResponse<Map<String, String>>> result = userController
                            .loginUser(fakeReqUser);
                    assertTrue(result != null);
                    assertTrue(result.getStatusCode().is5xxServerError());
                    assertEquals(result.getBody().getStatus(), "error");
                }

                // Auth token gagal disimpan dengan tidak terdapat token lama
                {
                    Mockito.when(authTokenService.findUserToken(Mockito.any(UUID.class), Mockito.anyString()))
                            .thenReturn(null);

                    Mockito.when(authTokenService.createAuthToken(Mockito.any(AuthToken.class))).thenReturn(null);

                    ResponseEntity<ApiResponse<Map<String, String>>> result = userController
                            .loginUser(fakeReqUser);
                    assert (result != null);
                    assert (result.getStatusCode().is5xxServerError());
                    assert (result.getBody().getStatus().equals("error"));
                }

                // Berhasil login
                {
                    Mockito.when(authTokenService.findUserToken(Mockito.any(UUID.class),
                            Mockito.anyString()))
                            .thenReturn(null);

                    Mockito.when(authTokenService.createAuthToken(Mockito.any(AuthToken.class)))
                            .thenReturn(fakeAuthToken);

                    ResponseEntity<ApiResponse<Map<String, String>>> result = userController
                            .loginUser(fakeReqUser);
                    assert (result != null);
                    assert (result.getStatusCode().is2xxSuccessful());
                    assert (result.getBody().getStatus().equals("success"));
                }

            }
        }

        User authUser = new User("Auth User", "user@example.com", "password123");
        authUser.setId(UUID.randomUUID());

        // Menguji method getUserInfo
        {
            // Tidak terautentikasi
            {
                userController.authContext.setAuthUser(null);

                ResponseEntity<ApiResponse<Map<String, User>>> result = userController.getUserInfo();
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            // Berhasil mendapatkan info user
            {
                userController.authContext.setAuthUser(authUser);

                ResponseEntity<ApiResponse<Map<String, User>>> result = userController.getUserInfo();
                assert (result != null);
                assert (result.getStatusCode().is2xxSuccessful());
                assert (result.getBody().getStatus().equals("success"));
            }
        }

        // Menguji method updateUser
        {
            // Tidak terautentikasi
            {
                userController.authContext.setAuthUser(null);

                ResponseEntity<ApiResponse<User>> result = userController.updateUser(authUser);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            // Data tidal valid
            {
                userController.authContext.setAuthUser(authUser);

                List<User> invalidUsers = List.of(
                        // Nama Null
                        new User(null, "user@example.com", ""),
                        // Nama Kosong
                        new User("", "user@example.com", ""),
                        // Email Null
                        new User("Auth User", null, ""),
                        // Email Kosong
                        new User("Auth User", "", ""));

                for (User reqUser : invalidUsers) {
                    ResponseEntity<ApiResponse<User>> result = userController.updateUser(reqUser);
                    assert (result != null);
                    assert (result.getStatusCode().is4xxClientError());
                    assert (result.getBody().getStatus().equals("fail"));
                }
            }

            // Gagal update user karena user tidak ditemukan
            {
                Mockito.when(userService.updateUser(
                        Mockito.any(UUID.class),
                        Mockito.any(String.class),
                        Mockito.any(String.class)))
                        .thenReturn(null);

                ResponseEntity<ApiResponse<User>> result = userController.updateUser(authUser);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            // Berhasil mengupdate user
            {
                Mockito.when(userService.updateUser(
                        Mockito.any(UUID.class),
                        Mockito.any(String.class),
                        Mockito.any(String.class)))
                        .thenReturn(authUser);

                ResponseEntity<ApiResponse<User>> result = userController.updateUser(authUser);
                assert (result != null);
                assert (result.getStatusCode().is2xxSuccessful());
                assert (result.getBody().getStatus().equals("success"));
            }
        }

        // Menguji method updateUserPassword
        {
            Map<String, String> passwordPayload = Map.of(
                    "password", "oldpassword123",
                    "newPassword", "newpassword123");

            // Tidak terautentikasi
            {
                userController.authContext.setAuthUser(null);

                ResponseEntity<ApiResponse<Void>> result = userController
                        .updateUserPassword(passwordPayload);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            userController.authContext.setAuthUser(authUser);

            // Data tidal valid
            {
                List<Map<String, String>> invalidPayloads = List.of(
                        // Old password Null
                        Map.of(
                                "no-password", "",
                                "newPassword", "newpassword123"),
                        // Old password Kosong
                        Map.of(
                                "password", "",
                                "newPassword", "newpassword123"),
                        // New password Null
                        Map.of(
                                "password", "oldpassword123",
                                "no-newPassword", ""),
                        // New password Kosong
                        Map.of(
                                "password", "oldpassword123",
                                "newPassword", ""));

                for (Map<String, String> payload : invalidPayloads) {
                    ResponseEntity<ApiResponse<Void>> result = userController
                            .updateUserPassword(payload);
                    assert (result != null);
                    assert (result.getStatusCode().is4xxClientError());
                    assert (result.getBody().getStatus().equals("fail"));
                }
            }

            // Password lama salah
            {
                authUser.setPassword(new BCryptPasswordEncoder().encode("correctOldPassword"));
                ResponseEntity<ApiResponse<Void>> result = userController
                        .updateUserPassword(passwordPayload);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            // User tidak ditemukan saat mengupdate password
            {
                authUser.setPassword(new BCryptPasswordEncoder().encode("oldpassword123"));

                Mockito.when(userService.updatePassword(
                        Mockito.any(UUID.class),
                        Mockito.any(String.class)))
                        .thenReturn(null);

                ResponseEntity<ApiResponse<Void>> result = userController
                        .updateUserPassword(passwordPayload);
                assert (result != null);
                assert (result.getStatusCode().is4xxClientError());
                assert (result.getBody().getStatus().equals("fail"));
            }

            // Berhasil mengupdate password
            {
                authUser.setPassword(new BCryptPasswordEncoder().encode("oldpassword123"));

                Mockito.when(userService.updatePassword(
                        Mockito.any(UUID.class),
                        Mockito.any(String.class)))
                        .thenReturn(authUser);

                ResponseEntity<ApiResponse<Void>> result = userController
                        .updateUserPassword(passwordPayload);
                assert (result != null);
                assert (result.getStatusCode().is2xxSuccessful());
                assert (result.getBody().getStatus().equals("success"));
            }
        }
    }
}
