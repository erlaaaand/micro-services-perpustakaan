package com.perpustakaan.service_anggota.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void testHandleValidationException() {
        // Mock MethodArgumentNotValidException
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("anggotaRequest", "nomorAnggota", "Nomor anggota tidak boleh kosong");
        FieldError fieldError2 = new FieldError("anggotaRequest", "email", "Format email tidak valid");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Validation Failed");
        
        @SuppressWarnings("unchecked")
        Map<String, String> validationErrors = (Map<String, String>) response.getBody().get("validationErrors");
        assertThat(validationErrors).containsKey("nomorAnggota");
        assertThat(validationErrors).containsKey("email");
        assertThat(validationErrors.get("nomorAnggota")).isEqualTo("Nomor anggota tidak boleh kosong");
    }

    @Test
    @DisplayName("Should handle validation exception with null message")
    void testHandleValidationException_NullMessage() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("anggotaRequest", "nama", null);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError));

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        
        @SuppressWarnings("unchecked")
        Map<String, String> validationErrors = (Map<String, String>) response.getBody().get("validationErrors");
        assertThat(validationErrors.get("nama")).isEqualTo("Invalid value");
    }

    @Test
    @DisplayName("Should handle RuntimeException")
    void testHandleRuntimeException() {
        RuntimeException ex = new RuntimeException("Something went wrong");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRuntimeException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("Internal Server Error");
        assertThat(response.getBody().get("message")).isEqualTo("Something went wrong");
        assertThat(response.getBody().get("timestamp")).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @DisplayName("Should handle RestClientException")
    void testHandleRestClientException() {
        RestClientException ex = new RestClientException("Service unavailable");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRestClientException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(503);
        assertThat(response.getBody().get("error")).isEqualTo("Service Unavailable");
        assertThat(response.getBody().get("message")).asString()
            .contains("Unable to communicate with required service");
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException")
    void testHandleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument provided");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleIllegalArgumentException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(400);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("Invalid argument provided");
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void testHandleGenericException() {
        Exception ex = new Exception("Unexpected error occurred");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo(500);
        assertThat(response.getBody().get("error")).isEqualTo("Unexpected Error");
        assertThat(response.getBody().get("message")).isEqualTo("Unexpected error occurred");
    }

    @Test
    @DisplayName("Should include timestamp in all error responses")
    void testErrorResponsesIncludeTimestamp() {
        RuntimeException ex = new RuntimeException("Test");

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleRuntimeException(ex);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsKey("timestamp");
        assertThat(response.getBody().get("timestamp")).isInstanceOf(LocalDateTime.class);
    }

    @Test
    @DisplayName("Should handle duplicate field errors in validation")
    void testHandleValidationException_DuplicateFields() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("anggotaRequest", "nama", "Nama tidak boleh kosong");
        FieldError fieldError2 = new FieldError("anggotaRequest", "nama", "Nama harus antara 3-100 karakter");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationException(ex);

        @SuppressWarnings("unchecked")
        Map<String, String> validationErrors = (Map<String, String>) response.getBody().get("validationErrors");
        
        // Should keep the first error message
        assertThat(validationErrors.get("nama")).isEqualTo("Nama tidak boleh kosong");
    }
}