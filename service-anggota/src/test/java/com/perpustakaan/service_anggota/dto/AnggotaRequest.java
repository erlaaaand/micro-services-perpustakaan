package com.perpustakaan.service_anggota.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AnggotaRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create AnggotaRequest with no-args constructor")
    void testNoArgsConstructor() {
        AnggotaRequest request = new AnggotaRequest();
        assertThat(request).isNotNull();
    }

    @Test
    @DisplayName("Should set and get all fields")
    void testGettersAndSetters() {
        AnggotaRequest request = new AnggotaRequest();
        
        request.setNomorAnggota("A001");
        request.setNama("John Doe");
        request.setAlamat("Jl. Test No. 123");
        request.setEmail("john@test.com");

        assertThat(request.getNomorAnggota()).isEqualTo("A001");
        assertThat(request.getNama()).isEqualTo("John Doe");
        assertThat(request.getAlamat()).isEqualTo("Jl. Test No. 123");
        assertThat(request.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void testValidation_ValidData() {
        AnggotaRequest request = createValidRequest();

        Set<ConstraintViolation<AnggotaRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation when nomorAnggota is blank")
    void testValidation_BlankNomorAnggota() {
        AnggotaRequest request = createValidRequest();
        request.setNomorAnggota("");

        Set<ConstraintViolation<AnggotaRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("nomorAnggota"));
    }

    @Test
    @DisplayName("Should fail validation when nomorAnggota is too short")
    void testValidation_ShortNomorAnggota() {
        AnggotaRequest request = createValidRequest();
        request.setNomorAnggota("A1");

        Set<ConstraintViolation<AnggotaRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("nomorAnggota");
    }

    @Test
    @DisplayName("Should fail validation when nomorAnggota is too long")
    void testValidation_LongNomorAnggota() {
        AnggotaRequest request = createValidRequest();
        request.setNomorAnggota("A123456789012345678901");

        Set<ConstraintViolation<AnggotaRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("nomorAnggota");
    }

    @Test
    @DisplayName("Should fail validation when nama is blank")
    void testValidation_BlankNama() {
        AnggotaRequest request = createValidRequest();
        request.setNama("");

        Set<ConstraintViolation<AnggotaRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("nama"));
    }

    @Test
    @DisplayName("Should fail validation when nama is too short")
    void testValidation_ShortNama() {
        AnggotaRequest request = createValidRequest();
        request.setNama("Jo");

        Set<ConstraintViolation<AnggotaRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("nama");
    }

    @Test
    @DisplayName("Should fail validation when alamat is blank")
    void testValidation_BlankAlamat() {
        AnggotaRequest request = createValidRequest();
        request.setAlamat("");

        Set<ConstraintViolation<AnggotaRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("alamat"));
    }

    @Test
    @DisplayName("Should fail validation when email is blank")
    void testValidation_BlankEmail() {
        AnggotaRequest request = createValidRequest();
        request.setEmail("");

        Set<ConstraintViolation<AnggotaRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("Should fail validation when email format is invalid")
    void testValidation_InvalidEmail() {
        AnggotaRequest request = createValidRequest();
        request.setEmail("invalid-email");

        Set<ConstraintViolation<AnggotaRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("email");
    }

    @Test
    @DisplayName("Should fail validation when all fields are blank")
    void testValidation_AllFieldsBlank() {
        AnggotaRequest request = new AnggotaRequest();

        Set<ConstraintViolation<AnggotaRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(4);
    }

    @Test
    @DisplayName("Should accept valid email formats")
    void testValidation_ValidEmailFormats() {
        String[] validEmails = {
            "test@test.com",
            "user.name@example.com",
            "user+tag@example.co.id",
            "123@test.com"
        };

        for (String email : validEmails) {
            AnggotaRequest request = createValidRequest();
            request.setEmail(email);

            Set<ConstraintViolation<AnggotaRequest>> violations = validator.validate(request);
            assertThat(violations).as("Email %s should be valid", email).isEmpty();
        }
    }

    @Test
    @DisplayName("Should reject invalid email formats")
    void testValidation_InvalidEmailFormats() {
        String[] invalidEmails = {
            "invalid",
            "@test.com",
            "test@",
            "test..email@test.com",
            "test @test.com"
        };

        for (String email : invalidEmails) {
            AnggotaRequest request = createValidRequest();
            request.setEmail(email);

            Set<ConstraintViolation<AnggotaRequest>> violations = validator.validate(request);
            assertThat(violations).as("Email %s should be invalid", email).isNotEmpty();
        }
    }

    @Test
    @DisplayName("Should handle edge case for nomor anggota length")
    void testValidation_EdgeCaseNomorAnggota() {
        // Minimum valid length (3 characters)
        AnggotaRequest request1 = createValidRequest();
        request1.setNomorAnggota("A01");
        assertThat(validator.validate(request1)).isEmpty();

        // Maximum valid length (20 characters)
        AnggotaRequest request2 = createValidRequest();
        request2.setNomorAnggota("A0123456789012345678".substring(0, 20));
        assertThat(validator.validate(request2)).isEmpty();
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
        AnggotaRequest request1 = createValidRequest();
        AnggotaRequest request2 = createValidRequest();

        assertThat(request1).isEqualTo(request2);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("Should generate toString")
    void testToString() {
        AnggotaRequest request = createValidRequest();

        String toString = request.toString();
        assertThat(toString).contains("A001");
        assertThat(toString).contains("John Doe");
    }

    @Test
    @DisplayName("Should validate null fields")
    void testValidation_NullFields() {
        AnggotaRequest request = new AnggotaRequest();
        request.setNomorAnggota(null);
        request.setNama(null);
        request.setAlamat(null);
        request.setEmail(null);

        Set<ConstraintViolation<AnggotaRequest>> violations = validator.validate(request);
        assertThat(violations).hasSize(4);
    }

    private AnggotaRequest createValidRequest() {
        AnggotaRequest request = new AnggotaRequest();
        request.setNomorAnggota("A001");
        request.setNama("John Doe");
        request.setAlamat("Jl. Test No. 123");
        request.setEmail("john@test.com");
        return request;
    }
}