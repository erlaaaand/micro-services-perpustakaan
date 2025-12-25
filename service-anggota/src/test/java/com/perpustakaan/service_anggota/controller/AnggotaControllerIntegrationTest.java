package com.perpustakaan.service_anggota.controller;

import com.perpustakaan.service_anggota.dto.AnggotaRequest;
import com.perpustakaan.service_anggota.entity.Anggota;
import com.perpustakaan.service_anggota.repository.AnggotaRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnggotaControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AnggotaRepository anggotaRepository;

    private String baseUrl;
    private static Long createdAnggotaId;

    @BeforeAll
    void setupAll() {
        RestAssured.baseURI = "http://localhost";
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        baseUrl = "/api/anggota";
    }

    @AfterEach
    void cleanUp() {
        // Clean up after each test to ensure isolation
        if (anggotaRepository != null) {
            anggotaRepository.deleteAll();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should create new anggota successfully")
    void testCreateAnggota_Success() {
        AnggotaRequest request = createValidRequest("A001", "John Doe", "Jl. Test No. 123", "john@test.com");

        Long id = given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(baseUrl)
        .then()
            .statusCode(HttpStatus.CREATED.value())
            .body("id", notNullValue())
            .body("nomorAnggota", equalTo("A001"))
            .body("nama", equalTo("John Doe"))
            .body("alamat", equalTo("Jl. Test No. 123"))
            .body("email", equalTo("john@test.com"))
            .extract()
            .path("id");

        createdAnggotaId = id;
        assertThat(id).isNotNull();

        // Verify in database
        Anggota saved = anggotaRepository.findById(id.longValue()).orElse(null);
        assertThat(saved).isNotNull();
        assertThat(saved.getNomorAnggota()).isEqualTo("A001");
    }

    @Test
    @Order(2)
    @DisplayName("Should get anggota by id successfully")
    void testGetAnggotaById_Success() {
        // Create test data
        Anggota anggota = saveTestAnggota("A002", "Jane Doe", "Jl. Test", "jane@test.com");

        given()
            .pathParam("id", anggota.getId())
        .when()
            .get(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(anggota.getId().intValue()))
            .body("nomorAnggota", equalTo("A002"))
            .body("nama", equalTo("Jane Doe"));
    }

    @Test
    @Order(3)
    @DisplayName("Should return 404 when anggota not found")
    void testGetAnggotaById_NotFound() {
        given()
            .pathParam("id", 99999)
        .when()
            .get(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @Order(4)
    @DisplayName("Should get all anggota successfully")
    void testGetAllAnggota_Success() {
        // Create multiple test anggota
        saveTestAnggota("A003", "User 1", "Jl. 1", "user1@test.com");
        saveTestAnggota("A004", "User 2", "Jl. 2", "user2@test.com");
        saveTestAnggota("A005", "User 3", "Jl. 3", "user3@test.com");

        given()
        .when()
            .get(baseUrl)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("$", hasSize(3))
            .body("[0].id", notNullValue())
            .body("[1].id", notNullValue())
            .body("[2].id", notNullValue());
    }

    @Test
    @Order(5)
    @DisplayName("Should return empty list when no anggota exists")
    void testGetAllAnggota_EmptyList() {
        given()
        .when()
            .get(baseUrl)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("$", hasSize(0));
    }

    @Test
    @Order(6)
    @DisplayName("Should update anggota successfully")
    void testUpdateAnggota_Success() {
        Anggota anggota = saveTestAnggota("A006", "Old Name", "Old Address", "old@test.com");

        AnggotaRequest updateRequest = createValidRequest(
            "A006-UPD", "Updated Name", "Updated Address", "updated@test.com"
        );

        given()
            .contentType(ContentType.JSON)
            .pathParam("id", anggota.getId())
            .body(updateRequest)
        .when()
            .put(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("id", equalTo(anggota.getId().intValue()))
            .body("nomorAnggota", equalTo("A006-UPD"))
            .body("nama", equalTo("Updated Name"))
            .body("alamat", equalTo("Updated Address"))
            .body("email", equalTo("updated@test.com"));

        // Verify in database
        Anggota updated = anggotaRepository.findById(anggota.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getNama()).isEqualTo("Updated Name");
    }

    @Test
    @Order(7)
    @DisplayName("Should return 404 when updating non-existent anggota")
    void testUpdateAnggota_NotFound() {
        AnggotaRequest request = createValidRequest("A999", "Test", "Test", "test@test.com");

        given()
            .contentType(ContentType.JSON)
            .pathParam("id", 99999)
            .body(request)
        .when()
            .put(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @Order(8)
    @DisplayName("Should delete anggota successfully")
    void testDeleteAnggota_Success() {
        Anggota anggota = saveTestAnggota("A007", "To Delete", "Jl. Delete", "delete@test.com");
        Long id = anggota.getId();

        given()
            .pathParam("id", id)
        .when()
            .delete(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.NO_CONTENT.value());

        // Verify deletion
        given()
            .pathParam("id", id)
        .when()
            .get(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());

        // Verify in database
        assertThat(anggotaRepository.findById(id)).isEmpty();
    }

    @Test
    @Order(9)
    @DisplayName("Should return 404 when deleting non-existent anggota")
    void testDeleteAnggota_NotFound() {
        given()
            .pathParam("id", 99999)
        .when()
            .delete(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    @Order(10)
    @DisplayName("Should return 400 with validation errors for empty request")
    void testValidation_EmptyRequest() {
        AnggotaRequest emptyRequest = new AnggotaRequest();

        given()
            .contentType(ContentType.JSON)
            .body(emptyRequest)
        .when()
            .post(baseUrl)
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("status", equalTo(400))
            .body("error", equalTo("Validation Failed"))
            .body("validationErrors", notNullValue())
            .body("validationErrors.nomorAnggota", notNullValue())
            .body("validationErrors.nama", notNullValue())
            .body("validationErrors.alamat", notNullValue())
            .body("validationErrors.email", notNullValue());
    }

    @Test
    @Order(11)
    @DisplayName("Should return 400 when nomor anggota is too short")
    void testValidation_ShortNomorAnggota() {
        AnggotaRequest request = createValidRequest("A1", "Test", "Test", "test@test.com");

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(baseUrl)
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("validationErrors.nomorAnggota", notNullValue());
    }

    @Test
    @Order(12)
    @DisplayName("Should return 400 when email format is invalid")
    void testValidation_InvalidEmail() {
        AnggotaRequest request = createValidRequest("A008", "Test", "Test", "invalid-email");

        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post(baseUrl)
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("validationErrors.email", notNullValue());
    }

    @Test
    @Order(13)
    @DisplayName("Should return 400 when creating duplicate nomor anggota")
    void testCreateAnggota_DuplicateNomor() {
        saveTestAnggota("A009", "First", "Address", "first@test.com");

        AnggotaRequest duplicate = createValidRequest("A009", "Second", "Address", "second@test.com");

        given()
            .contentType(ContentType.JSON)
            .body(duplicate)
        .when()
            .post(baseUrl)
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("message", containsString("Nomor anggota sudah digunakan"));
    }

    @Test
    @Order(14)
    @DisplayName("Should handle concurrent create requests")
    void testConcurrentRequests() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            Future<?> future = executor.submit(() -> {
                try {
                    AnggotaRequest request = createValidRequest(
                        "CONC" + index,
                        "Concurrent " + index,
                        "Jl. Concurrent " + index,
                        "concurrent" + index + "@test.com"
                    );

                    int statusCode = given()
                        .contentType(ContentType.JSON)
                        .body(request)
                    .when()
                        .post(baseUrl)
                    .then()
                        .extract()
                        .statusCode();

                    if (statusCode == HttpStatus.CREATED.value()) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
            futures.add(future);
        }

        // Wait for all threads to complete
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Wait for all futures
        for (Future<?> future : futures) {
            future.get(5, TimeUnit.SECONDS);
        }

        // Verify that all requests succeeded
        assertThat(successCount.get()).isEqualTo(threadCount);

        // Verify in database
        List<Anggota> allAnggota = anggotaRepository.findAll();
        long concurrentCount = allAnggota.stream()
            .filter(a -> a.getNomorAnggota().startsWith("CONC"))
            .count();
        assertThat(concurrentCount).isEqualTo(threadCount);
    }

    @Test
    @Order(15)
    @DisplayName("Should handle large dataset")
    void testLargeDataset() {
        // Create multiple anggota
        for (int i = 0; i < 20; i++) {
            saveTestAnggota(
                String.format("BULK%03d", i),
                "User " + i,
                "Address " + i,
                "user" + i + "@test.com"
            );
        }

        given()
        .when()
            .get(baseUrl)
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("$", hasSize(20));
    }

    @Test
    @Order(16)
    @DisplayName("Should update with same nomor anggota")
    void testUpdateAnggota_SameNomor() {
        Anggota anggota = saveTestAnggota("A010", "Original", "Original", "original@test.com");

        AnggotaRequest updateRequest = createValidRequest(
            "A010", // Same nomor
            "Updated Name Only",
            "Updated Address",
            "updated@test.com"
        );

        given()
            .contentType(ContentType.JSON)
            .pathParam("id", anggota.getId())
            .body(updateRequest)
        .when()
            .put(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.OK.value())
            .body("nama", equalTo("Updated Name Only"));
    }

    @Test
    @Order(17)
    @DisplayName("Should return 400 when updating with duplicate nomor anggota")
    void testUpdateAnggota_DuplicateNomor() {
        Anggota anggota1 = saveTestAnggota("A011", "User 1", "Address 1", "user1@test.com");
        Anggota anggota2 = saveTestAnggota("A012", "User 2", "Address 2", "user2@test.com");

        // Try to update anggota2 with anggota1's nomor
        AnggotaRequest updateRequest = createValidRequest(
            "A011", // Duplicate nomor
            "Updated User 2",
            "Updated Address",
            "updated@test.com"
        );

        given()
            .contentType(ContentType.JSON)
            .pathParam("id", anggota2.getId())
            .body(updateRequest)
        .when()
            .put(baseUrl + "/{id}")
        .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("message", containsString("Nomor anggota sudah digunakan"));
    }

    // Helper methods
    private AnggotaRequest createValidRequest(String nomor, String nama, String alamat, String email) {
        AnggotaRequest request = new AnggotaRequest();
        request.setNomorAnggota(nomor);
        request.setNama(nama);
        request.setAlamat(alamat);
        request.setEmail(email);
        return request;
    }

    private Anggota saveTestAnggota(String nomor, String nama, String alamat, String email) {
        Anggota anggota = new Anggota();
        anggota.setNomorAnggota(nomor);
        anggota.setNama(nama);
        anggota.setAlamat(alamat);
        anggota.setEmail(email);
        return anggotaRepository.save(anggota);
    }
}