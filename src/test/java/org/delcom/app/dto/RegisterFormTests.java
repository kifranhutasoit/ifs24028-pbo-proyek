package org.delcom.app.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.*;

class RegisterFormTest {

    private RegisterForm registerForm;
    private Validator validator;

    @BeforeEach
    void setUp() {
        registerForm = new RegisterForm();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Default constructor membuat objek dengan nilai default")
    void defaultConstructor_CreatesObjectWithDefaultValues() {
        assertNull(registerForm.getName());
        assertNull(registerForm.getEmail());
        assertNull(registerForm.getPassword());
    }

    @Test
    @DisplayName("Setter dan Getter untuk name bekerja dengan benar")
    void setterAndGetter_Name_WorksCorrectly() {
        String name = "John Doe";
        registerForm.setName(name);
        assertEquals(name, registerForm.getName());
    }

    @Test
    @DisplayName("Setter dan Getter untuk email bekerja dengan benar")
    void setterAndGetter_Email_WorksCorrectly() {
        String email = "john@example.com";
        registerForm.setEmail(email);
        assertEquals(email, registerForm.getEmail());
    }

    @Test
    @DisplayName("Setter dan Getter untuk password bekerja dengan benar")
    void setterAndGetter_Password_WorksCorrectly() {
        String password = "password123";
        registerForm.setPassword(password);
        assertEquals(password, registerForm.getPassword());
    }

    @Test
    @DisplayName("Validation berhasil ketika semua field valid")
    void validation_Success_WhenAllFieldsValid() {
        registerForm.setName("John Doe");
        registerForm.setEmail("john@example.com");
        registerForm.setPassword("password123");

        var violations = validator.validate(registerForm);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Validation gagal ketika name null")
    void validation_Fail_WhenNameIsNull() {
        registerForm.setName(null);
        registerForm.setEmail("john@example.com");
        registerForm.setPassword("password123");

        var violations = validator.validate(registerForm);
        assertEquals(1, violations.size());
        assertEquals("Nama harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika name empty string")
    void validation_Fail_WhenNameIsEmpty() {
        registerForm.setName("");
        registerForm.setEmail("john@example.com");
        registerForm.setPassword("password123");

        var violations = validator.validate(registerForm);
        assertEquals(1, violations.size());
        assertEquals("Nama harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika name blank")
    void validation_Fail_WhenNameIsBlank() {
        registerForm.setName("   ");
        registerForm.setEmail("john@example.com");
        registerForm.setPassword("password123");

        var violations = validator.validate(registerForm);
        assertEquals(1, violations.size());
        assertEquals("Nama harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika email null")
    void validation_Fail_WhenEmailIsNull() {
        registerForm.setName("John Doe");
        registerForm.setEmail(null);
        registerForm.setPassword("password123");

        var violations = validator.validate(registerForm);
        assertEquals(1, violations.size());
        assertEquals("Email harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika email empty string")
    void validation_Fail_WhenEmailIsEmpty() {
        registerForm.setName("John Doe");
        registerForm.setEmail("");
        registerForm.setPassword("password123");

        var violations = validator.validate(registerForm);
        assertEquals(1, violations.size());
        assertEquals("Email harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika format email tidak valid")
    void validation_Fail_WhenEmailFormatInvalid() {
        registerForm.setName("John Doe");
        registerForm.setEmail("invalid-email");
        registerForm.setPassword("password123");

        var violations = validator.validate(registerForm);
        assertEquals(1, violations.size());
        assertEquals("Format email tidak valid", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika password null")
    void validation_Fail_WhenPasswordIsNull() {
        registerForm.setName("John Doe");
        registerForm.setEmail("john@example.com");
        registerForm.setPassword(null);

        var violations = validator.validate(registerForm);
        assertEquals(1, violations.size());
        assertEquals("Kata sandi harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika password empty string")
    void validation_Fail_WhenPasswordIsEmpty() {
        registerForm.setName("John Doe");
        registerForm.setEmail("john@example.com");
        registerForm.setPassword("");

        var violations = validator.validate(registerForm);
        assertEquals(1, violations.size());
        assertEquals("Kata sandi harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika password blank")
    void validation_Fail_WhenPasswordIsBlank() {
        registerForm.setName("John Doe");
        registerForm.setEmail("john@example.com");
        registerForm.setPassword("   ");

        var violations = validator.validate(registerForm);
        assertEquals(1, violations.size());
        assertEquals("Kata sandi harus diisi", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Validation gagal ketika semua field null")
    void validation_Fail_WhenAllFieldsNull() {
        registerForm.setName(null);
        registerForm.setEmail(null);
        registerForm.setPassword(null);

        var violations = validator.validate(registerForm);
        assertEquals(3, violations.size());

        var violationMessages = violations.stream()
                .map(violation -> violation.getMessage())
                .toList();

        assertTrue(violationMessages.contains("Nama harus diisi"));
        assertTrue(violationMessages.contains("Email harus diisi"));
        assertTrue(violationMessages.contains("Kata sandi harus diisi"));
    }

    @Test
    @DisplayName("Validation gagal ketika name dan email invalid")
    void validation_Fail_WhenNameAndEmailInvalid() {
        registerForm.setName("");
        registerForm.setEmail("invalid-email");
        registerForm.setPassword("password123");

        var violations = validator.validate(registerForm);
        assertEquals(2, violations.size());

        var violationMessages = violations.stream()
                .map(violation -> violation.getMessage())
                .toList();

        assertTrue(violationMessages.contains("Nama harus diisi"));
        assertTrue(violationMessages.contains("Format email tidak valid"));
    }

    @Test
    @DisplayName("Validation gagal ketika email dan password invalid")
    void validation_Fail_WhenEmailAndPasswordInvalid() {
        registerForm.setName("John Doe");
        registerForm.setEmail("invalid-email");
        registerForm.setPassword("");

        var violations = validator.validate(registerForm);
        assertEquals(2, violations.size());

        var violationMessages = violations.stream()
                .map(violation -> violation.getMessage())
                .toList();

        assertTrue(violationMessages.contains("Format email tidak valid"));
        assertTrue(violationMessages.contains("Kata sandi harus diisi"));
    }

    @Test
    @DisplayName("Validation gagal ketika name dan password invalid")
    void validation_Fail_WhenNameAndPasswordInvalid() {
        registerForm.setName("");
        registerForm.setEmail("john@example.com");
        registerForm.setPassword("");

        var violations = validator.validate(registerForm);
        assertEquals(2, violations.size());

        var violationMessages = violations.stream()
                .map(violation -> violation.getMessage())
                .toList();

        assertTrue(violationMessages.contains("Nama harus diisi"));
        assertTrue(violationMessages.contains("Kata sandi harus diisi"));
    }

    @Test
    @DisplayName("Email dengan format valid diterima")
    void email_WithValidFormat_IsAccepted() {
        String[] validEmails = {
                "user@example.com",
                "firstname.lastname@example.com",
                "email@subdomain.example.com",
                "firstname+lastname@example.com",
                "email@example.name"
        };

        for (String validEmail : validEmails) {
            registerForm.setName("John Doe");
            registerForm.setEmail(validEmail);
            registerForm.setPassword("password123");

            var violations = validator.validate(registerForm);
            assertTrue(violations.isEmpty(), "Should accept valid email: " + validEmail);
        }
    }

    @Test
    @DisplayName("Email dengan format invalid ditolak")
    void email_WithInvalidFormat_IsRejected() {
        String[] invalidEmails = {
                "plainaddress",
                "@no-local-part.com",
                "email.domain.com",
                "email@domain@domain.com",
                ".email@domain.com"
        };

        for (String invalidEmail : invalidEmails) {
            registerForm.setName("John Doe");
            registerForm.setEmail(invalidEmail);
            registerForm.setPassword("password123");

            var violations = validator.validate(registerForm);
            assertEquals(1, violations.size());
            assertEquals("Format email tidak valid", violations.iterator().next().getMessage());
        }
    }
}