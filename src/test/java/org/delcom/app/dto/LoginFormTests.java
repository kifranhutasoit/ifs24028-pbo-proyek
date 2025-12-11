package org.delcom.app.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.*;

class LoginFormTest {

    private LoginForm loginForm;
    private Validator validator;

    @BeforeEach
    void setUp() {
        loginForm = new LoginForm();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Default constructor membuat objek dengan nilai default")
    void defaultConstructor_CreatesObjectWithDefaultValues() {
        assertNull(loginForm.getEmail());
        assertNull(loginForm.getPassword());
        assertFalse(loginForm.isRememberMe());
    }

    @Test
    @DisplayName("Setter dan Getter untuk email bekerja dengan benar")
    void setterAndGetter_Email_WorksCorrectly() {
        String email = "test@example.com";
        loginForm.setEmail(email);
        assertEquals(email, loginForm.getEmail());
    }

    @Test
    @DisplayName("Setter dan Getter untuk password bekerja dengan benar")
    void setterAndGetter_Password_WorksCorrectly() {
        String password = "password123";
        loginForm.setPassword(password);
        assertEquals(password, loginForm.getPassword());
    }

    @Test
    @DisplayName("Setter dan Getter untuk rememberMe bekerja dengan benar")
    void setterAndGetter_RememberMe_WorksCorrectly() {
        loginForm.setRememberMe(true);
        assertTrue(loginForm.isRememberMe());

        loginForm.setRememberMe(false);
        assertFalse(loginForm.isRememberMe());
    }

    @Test
    @DisplayName("Validation berhasil ketika semua field valid")
    void validation_Success_WhenAllFieldsValid() {
        loginForm.setEmail("user@example.com");
        loginForm.setPassword("password123");

        var violations = validator.validate(loginForm);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Validation gagal ketika email null")
    void validation_Fail_WhenEmailIsNull() {
        loginForm.setEmail(null);
        loginForm.setPassword("password123");

        var violations = validator.validate(loginForm);
        assertEquals(1, violations.size());
        assertEquals("Email harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika email empty string")
    void validation_Fail_WhenEmailIsEmpty() {
        loginForm.setEmail("");
        loginForm.setPassword("password123");

        var violations = validator.validate(loginForm);
        assertEquals(1, violations.size());
        assertEquals("Email harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika format email tidak valid")
    void validation_Fail_WhenEmailFormatInvalid() {
        loginForm.setEmail("invalid-email");
        loginForm.setPassword("password123");

        var violations = validator.validate(loginForm);
        assertEquals(1, violations.size());
        assertEquals("Format email tidak valid", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika password null")
    void validation_Fail_WhenPasswordIsNull() {
        loginForm.setEmail("user@example.com");
        loginForm.setPassword(null);

        var violations = validator.validate(loginForm);
        assertEquals(1, violations.size());
        assertEquals("Kata sandi harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika password empty string")
    void validation_Fail_WhenPasswordIsEmpty() {
        loginForm.setEmail("user@example.com");
        loginForm.setPassword("");

        var violations = validator.validate(loginForm);
        assertEquals(1, violations.size());
        assertEquals("Kata sandi harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika password blank")
    void validation_Fail_WhenPasswordIsBlank() {
        loginForm.setEmail("user@example.com");
        loginForm.setPassword("   ");

        var violations = validator.validate(loginForm);
        assertEquals(1, violations.size());
        assertEquals("Kata sandi harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika email dan password keduanya invalid")
    void validation_Fail_WhenBothEmailAndPasswordInvalid() {
        loginForm.setEmail("invalid-email");
        loginForm.setPassword("");

        var violations = validator.validate(loginForm);
        assertEquals(2, violations.size());

        var violationMessages = violations.stream()
                .map(violation -> violation.getMessage())
                .toList();

        assertTrue(violationMessages.contains("Format email tidak valid"));
        assertTrue(violationMessages.contains("Kata sandi harus diisi"));
    }

    @Test
    @DisplayName("RememberMe false tidak mempengaruhi validation")
    void rememberMeFalse_DoesNotAffectValidation() {
        loginForm.setEmail("user@example.com");
        loginForm.setPassword("password123");
        loginForm.setRememberMe(false);

        var violations = validator.validate(loginForm);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("RememberMe true tidak mempengaruhi validation")
    void rememberMeTrue_DoesNotAffectValidation() {
        loginForm.setEmail("user@example.com");
        loginForm.setPassword("password123");
        loginForm.setRememberMe(true);

        var violations = validator.validate(loginForm);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Email dengan format valid diterima")
    void email_WithValidFormat_IsAccepted() {
        String[] validEmails = {
                "user@example.com",
                "firstname.lastname@example.com",
                "email@subdomain.example.com",
                "firstname+lastname@example.com",
                "email@123.123.123.123",
                "email@[123.123.123.123]",
                "1234567890@example.com",
                "email@example-one.com",
                "_______@example.com",
                "email@example.name",
                "email@example.museum",
                "email@example.co.jp",
                "firstname-lastname@example.com"
        };

        for (String validEmail : validEmails) {
            loginForm.setEmail(validEmail);
            loginForm.setPassword("password123");

            var violations = validator.validate(loginForm);
            assertTrue(violations.isEmpty(), "Should accept valid email: " + validEmail);
        }
    }

    @Test
    @DisplayName("Email dengan format invalid ditolak")
    void email_WithInvalidFormat_IsRejected() {
        String[] invalidEmails = {
                "plainaddress",
                "@no-local-part.com",
                "Outlook Contact <outlook@contact.com>",
                "email.domain.com",
                "email@domain@domain.com",
                ".email@domain.com",
                "email.@domain.com",
                "email..email@domain.com",
                "email@domain..com"
        };

        for (String invalidEmail : invalidEmails) {
            loginForm.setEmail(invalidEmail);
            loginForm.setPassword("password123");

            var violations = validator.validate(loginForm);
            assertEquals(1, violations.size());
            assertEquals("Format email tidak valid", violations.iterator().next().getMessage());
        }
    }
}