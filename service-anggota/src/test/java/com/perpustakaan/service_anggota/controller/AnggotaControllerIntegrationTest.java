package com.perpustakaan.service_anggota.controller;

import com.perpustakaan.service_anggota.dto.AnggotaRequest;
import com.perpustakaan.service_anggota.repository.AnggotaRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(OrderAnnotation.class)
class AnggotaControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AnggotaRepository anggotaRepository;

    private String baseUrl;
    private static Long createdAnggotaId;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        baseUrl = "/api/anggota";
    }

    @AfterEach
    void cleanUp() {
        anggotaRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Create new anggota")
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
            .body("id", notNullValue())
            .body("nomorAnggota", equalTo("A001"))
            .extract()
            .path("id");
    }

    @Test
    @Order(2)
    @DisplayName("Get anggota by ID")
    void testGetAnggotaById() {
        given()
            .pathParam("id", createdAnggotaId)
        .when()
            .get(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(createdAnggotaId.intValue()))
            .body("nama", equalTo("John Doe"));
    }

    @Test
    @Order(3)
    @DisplayName("Get all anggota")
    void testGetAllAnggota() {
        given()
        .when()
            .get(baseUrl)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("$", hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    @Order(4)
    @DisplayName("Update anggota")
    void testUpdateAnggota() {
        AnggotaRequest request = new AnggotaRequest();
        request.setNomorAnggota("A001-UPD");
        request.setNama("John Updated");
        request.setAlamat("Jl. Update");
        request.setEmail("updated@test.com");

        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .pathParam("id", createdAnggotaId)
            .body(request)
        .when()
            .put(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("nama", equalTo("John Updated"));
    }

    @Test
    @Order(5)
    @DisplayName("Return 404 if anggota not found")
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
    @DisplayName("Delete anggota")
    void testDeleteAnggota() {
        given()
            .pathParam("id", createdAnggotaId)
        .when()
            .delete(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("Validation error on empty request")
    void testValidation() {
        given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(new AnggotaRequest())
        .when()
            .post(baseUrl)
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("Handle concurrent create requests")
    void testConcurrentRequests() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 5; i++) {
            final int idx = i;
            executor.submit(() -> {
                AnggotaRequest request = new AnggotaRequest();
                request.setNomorAnggota("AC" + idx);
                request.setNama("Concurrent " + idx);
                request.setAlamat("Jl. Concurrent");
                request.setEmail("c" + idx + "@test.com");

                given()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(request)
                .when()
                    .post(baseUrl)
                .then()
                    .statusCode(HttpStatus.CREATED.value());
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        given()
        .when()
            .get(baseUrl)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("$", hasSize(greaterThanOrEqualTo(5)));
    }
}
