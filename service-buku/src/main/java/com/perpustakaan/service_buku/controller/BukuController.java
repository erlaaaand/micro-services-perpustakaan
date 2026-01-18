package com.perpustakaan.service_buku.controller;

import com.perpustakaan.service_buku.cqrs.command.*;
import com.perpustakaan.service_buku.cqrs.handler.*;
import com.perpustakaan.service_buku.cqrs.query.*;
import com.perpustakaan.service_buku.dto.BukuRequest;
import com.perpustakaan.service_buku.entity.command.BukuWriteModel;
import com.perpustakaan.service_buku.entity.query.BukuReadModel;

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
@RequestMapping("/api/buku")
@Tag(name = "Buku Management", description = "APIs untuk manajemen data buku perpustakaan")
@RequiredArgsConstructor
public class BukuController {

    private static final Logger logger = LoggerFactory.getLogger(BukuController.class);

    private final BukuCommandHandler commandHandler;
    private final BukuQueryHandler queryHandler;

    @PostMapping
    @Operation(summary = "Tambah Buku Baru", description = "Menambahkan data buku baru ke perpustakaan.")
    public ResponseEntity<BukuWriteModel> createBuku(@Valid @RequestBody BukuRequest request) {
        logger.info("API REQUEST: Create Buku - Kode: [{}], Judul: [{}]", request.getKodeBuku(), request.getJudul());
        
        CreateBukuCommand command = new CreateBukuCommand(
            request.getKodeBuku(), request.getJudul(), request.getPengarang(),
            request.getPenerbit(), request.getTahunTerbit()
        );
        BukuWriteModel saved = commandHandler.handle(command);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lihat Detail Buku", description = "Mengambil data buku berdasarkan ID (Read Model).")
    public ResponseEntity<BukuReadModel> getBukuById(@PathVariable("id") UUID id) {
        logger.debug("API REQUEST: Get Buku By ID [{}]", id);
        
        GetBukuByIdQuery query = new GetBukuByIdQuery(id.toString());
        try {
            BukuReadModel buku = queryHandler.handle(query);
            return ResponseEntity.ok(buku);
        } catch (RuntimeException e) {
            logger.warn("API RESPONSE: Buku ID [{}] tidak ditemukan.", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "Lihat Semua Buku", description = "Menampilkan daftar buku dengan pagination.")
    public ResponseEntity<Page<BukuReadModel>> getAllBuku(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "judul") String sortBy) {
        
        logger.debug("API REQUEST: Get All Buku - Page: {}, Size: {}", page, size);
        GetAllBukuQuery query = new GetAllBukuQuery(page, size, sortBy);
        return ResponseEntity.ok(queryHandler.handle(query));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Buku", description = "Memperbarui informasi buku.")
    public ResponseEntity<BukuWriteModel> updateBuku(@PathVariable("id") UUID id, @Valid @RequestBody BukuRequest request) {
        logger.info("API REQUEST: Update Buku ID [{}]", id);
        
        UpdateBukuCommand command = new UpdateBukuCommand(
            id, request.getKodeBuku(), request.getJudul(), request.getPengarang(),
            request.getPenerbit(), request.getTahunTerbit()
        );
        
        try {
            BukuWriteModel updated = commandHandler.handle(command);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hapus Buku", description = "Menghapus buku dari sistem.")
    public ResponseEntity<Void> deleteBuku(@PathVariable("id") UUID id) {
        logger.info("API REQUEST: Delete Buku ID [{}]", id);
        
        DeleteBukuCommand command = new DeleteBukuCommand(id);
        try {
            commandHandler.handle(command);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}