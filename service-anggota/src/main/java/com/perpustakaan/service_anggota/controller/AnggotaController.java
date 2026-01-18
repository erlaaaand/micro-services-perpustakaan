package com.perpustakaan.service_anggota.controller;

import com.perpustakaan.service_anggota.cqrs.command.*;
import com.perpustakaan.service_anggota.cqrs.handler.*;
import com.perpustakaan.service_anggota.cqrs.query.*;
import com.perpustakaan.service_anggota.dto.AnggotaRequest;
import com.perpustakaan.service_anggota.entity.command.AnggotaWriteModel;
import com.perpustakaan.service_anggota.entity.query.AnggotaReadModel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/anggota")
@Tag(name = "Anggota Management", description = "APIs untuk manajemen data anggota perpustakaan")
@RequiredArgsConstructor
public class AnggotaController {

    private static final Logger logger = LoggerFactory.getLogger(AnggotaController.class);

    private final AnggotaCommandHandler commandHandler;
    private final AnggotaQueryHandler queryHandler;

    @PostMapping
    @Operation(summary = "Daftarkan Anggota Baru", description = "Membuat data anggota baru di sistem. Nomor anggota dan Email harus unik.")
    public ResponseEntity<AnggotaWriteModel> createAnggota(@Valid @RequestBody AnggotaRequest request) {
        logger.info("API REQUEST: Create Anggota - Nama: [{}], Nomor: [{}]", request.getNama(), request.getNomorAnggota());
        
        CreateAnggotaCommand command = new CreateAnggotaCommand(
            request.getNomorAnggota(), request.getNama(), request.getAlamat(), request.getEmail()
        );
        AnggotaWriteModel saved = commandHandler.handle(command);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Cari Anggota via ID", description = "Mengambil data detail anggota berdasarkan UUID dari Read Model (MongoDB).")
    public ResponseEntity<AnggotaReadModel> getAnggotaById(@PathVariable("id") UUID id) {
        logger.debug("API REQUEST: Get Anggota By ID [{}]", id);
        
        GetAnggotaByIdQuery query = new GetAnggotaByIdQuery(id.toString());
        try {
            AnggotaReadModel anggota = queryHandler.handle(query);
            return ResponseEntity.ok(anggota);
        } catch (RuntimeException e) {
            logger.warn("API RESPONSE: Anggota ID [{}] tidak ditemukan.", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Lihat Semua Anggota (Paging)", description = "Menampilkan daftar anggota dengan fitur pagination.")
    public ResponseEntity<Page<AnggotaReadModel>> getAllAnggota(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy) {
        
        logger.debug("API REQUEST: Get All Anggota - Page: {}, Size: {}", page, size);
        GetAllAnggotaQuery query = new GetAllAnggotaQuery(page, size, sortBy);
        Page<AnggotaReadModel> anggotaPage = queryHandler.handle(query);
        return ResponseEntity.ok(anggotaPage);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Data Anggota", description = "Memperbarui data anggota. Validasi duplikat email/nomor anggota akan tetap berjalan.")
    public ResponseEntity<AnggotaWriteModel> updateAnggota(@PathVariable("id") UUID id, @Valid @RequestBody AnggotaRequest request) {
        logger.info("API REQUEST: Update Anggota ID [{}]", id);
        
        UpdateAnggotaCommand command = new UpdateAnggotaCommand(
            id, request.getNomorAnggota(), request.getNama(), request.getAlamat(), request.getEmail()
        );
        try {
            AnggotaWriteModel updated = commandHandler.handle(command);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            // Log sudah ditangani di Handler, controller hanya meneruskan status
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hapus Anggota", description = "Menghapus anggota secara permanen dari sistem.")
    public ResponseEntity<Void> deleteAnggota(@PathVariable("id") UUID id) {
        logger.info("API REQUEST: Delete Anggota ID [{}]", id);
        
        DeleteAnggotaCommand command = new DeleteAnggotaCommand(id);
        try {
            commandHandler.handle(command);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}