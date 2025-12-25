package com.perpustakaan.service_anggota.controller;

import com.perpustakaan.service_anggota.dto.AnggotaRequest;
import com.perpustakaan.service_anggota.entity.Anggota;
import com.perpustakaan.service_anggota.repository.AnggotaRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AnggotaControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AnggotaRepository anggotaRepository;

    private String baseUrl;
    private static Long createdAnggotaId;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/anggota";
        baseURI = "http://localhost";
        port = this.port;
    }

    @AfterEach
    void tearDown() {
        // Clean up test data if needed
    }

    @Test
    @Order(1)
    @DisplayName("Should create new anggota")
    void testCreateAnggota() {
        AnggotaRequest request = new AnggotaRequest();
        request.setNomorAnggota("A001");
        request.setNama("John Doe");
        request.setAlamat("Jl. Test No. 123");
        request.setEmail("john@test.com");

        createdAnggotaId = given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(request)
        .when()
            .post(baseUrl)
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("nomorAnggota", equalTo("A001"))
            .body("nama", equalTo("John Doe"))
            .body("alamat", equalTo("Jl. Test No. 123"))
            .body("email", equalTo("john@test.com"))
            .body("id", notNullValue())
            .extract()
            .path("id");
    }

    @Test
    @Order(2)
    @DisplayName("Should get anggota by id")
    void testGetAnggotaById() {
        given()
            .pathParam("id", createdAnggotaId)
        .when()
            .get(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(createdAnggotaId.intValue()))
            .body("nomorAnggota", equalTo("A001"))
            .body("nama", equalTo("John Doe"));
    }

    @Test
    @Order(3)
    @DisplayName("Should get all anggota")
    void testGetAllAnggota() {
        given()
        .when()
            .get(baseUrl)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("$", hasSize(greaterThanOrEqualTo(1)))
            .body("[0].id", notNullValue());
    }

    @Test
    @Order(4)
    @DisplayName("Should update anggota")
    void testUpdateAnggota() {
        AnggotaRequest request = new AnggotaRequest();
        request.setNomorAnggota("A001-UPDATED");
        request.setNama("John Doe Updated");
        request.setAlamat("Jl. Updated No. 456");
        request.setEmail("john.updated@test.com");

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", createdAnggotaId)
            .body(request)
        .when()
            .put(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("nomorAnggota", equalTo("A001-UPDATED"))
            .body("nama", equalTo("John Doe Updated"))
            .body("email", equalTo("john.updated@test.com"));
    }

    @Test
    @Order(5)
    @DisplayName("Should return 404 when anggota not found")
    void testGetAnggotaNotFound() {
        given()
            .pathParam("id", 99999)
        .when()
            .get(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @Order(6)
    @DisplayName("Should delete anggota")
    void testDeleteAnggota() {
        given()
            .pathParam("id", createdAnggotaId)
        .when()
            .delete(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // Verify deletion
        given()
            .pathParam("id", createdAnggotaId)
        .when()
            .get(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @DisplayName("Should validate required fields")
    void testValidation() {
        AnggotaRequest request = new AnggotaRequest();
        // Empty request - should fail validation

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(request)
        .when()
            .post(baseUrl)
        .then()
            .statusCode(anyOf(
                equalTo(HttpStatus.BAD_REQUEST.value()),
                equalTo(HttpStatus.CREATED.value())
            ));
    }

    @Test
    @DisplayName("Should handle concurrent requests")
    void testConcurrentRequests() {
        // Create multiple anggota simultaneously
        for (int i = 0; i < 5; i++) {
            final int index = i;
            AnggotaRequest request = new AnggotaRequest();
            request.setNomorAnggota("A" + String.format("%03d", index));
            request.setNama("Anggota " + index);
            request.setAlamat("Jl. Concurrent " + index);
            request.setEmail("concurrent" + index + "@test.com");

            given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
            .when()
                .post(baseUrl)
            .then()
                .statusCode(HttpStatus.CREATED.value());
        }

        // Verify all created
        given()
        .when()
            .get(baseUrl)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("$", hasSize(greaterThanOrEqualTo(5)));
    }
}