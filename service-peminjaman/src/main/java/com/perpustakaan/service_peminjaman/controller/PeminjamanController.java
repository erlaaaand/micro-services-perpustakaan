package com.perpustakaan.service_peminjaman.controller;

import com.perpustakaan.service_peminjaman.cqrs.command.*;
import com.perpustakaan.service_peminjaman.cqrs.handler.*;
import com.perpustakaan.service_peminjaman.cqrs.query.*;
import com.perpustakaan.service_peminjaman.dto.PeminjamanRequest;
import com.perpustakaan.service_peminjaman.entity.command.PeminjamanWriteModel;
import com.perpustakaan.service_peminjaman.entity.query.PeminjamanReadModel;
import com.perpustakaan.service_peminjaman.vo.ResponseTemplateVO;

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
@RequestMapping("/api/peminjaman")
@Tag(name = "Peminjaman Management", description = "APIs untuk manajemen transaksi peminjaman buku")
@RequiredArgsConstructor
public class PeminjamanController {
    
    private static final Logger logger = LoggerFactory.getLogger(PeminjamanController.class);

    private final PeminjamanCommandHandler commandHandler;
    private final PeminjamanQueryHandler queryHandler;

    @PostMapping
    @Operation(summary = "Buat Peminjaman Baru", description = "Mencatat transaksi peminjaman buku.")
    public ResponseEntity<PeminjamanWriteModel> savePeminjaman(@Valid @RequestBody PeminjamanRequest request) {
        logger.info("API REQUEST: Create Peminjaman - Anggota: [{}], Buku: [{}]", request.getAnggotaId(), request.getBukuId());
        
        try {
            CreatePeminjamanCommand command = new CreatePeminjamanCommand(
                UUID.fromString(request.getAnggotaId()),
                UUID.fromString(request.getBukuId()),
                request.getTanggalPinjam(),
                request.getTanggalKembali(),
                request.getStatus()
            );
            
            PeminjamanWriteModel saved = commandHandler.handle(command);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.warn("API BAD REQUEST: {}", e.getMessage());
            return ResponseEntity.badRequest().build(); // Atau return error message object
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detail Peminjaman", description = "Mengambil data peminjaman beserta detail Anggota dan Buku (via RestTemplate).")
    public ResponseEntity<ResponseTemplateVO> getPeminjaman(@PathVariable("id") UUID id) {
        logger.debug("API REQUEST: Get Peminjaman ID [{}]", id);
        
        GetPeminjamanById query = new GetPeminjamanById(id.toString());
        ResponseTemplateVO response = queryHandler.handle(query);
        
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        logger.warn("API RESPONSE: Peminjaman ID [{}] tidak ditemukan.", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    @Operation(summary = "List Peminjaman", description = "Daftar semua transaksi dengan pagination.")
    public ResponseEntity<Page<PeminjamanReadModel>> getAllPeminjaman(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        logger.debug("API REQUEST: Get All Peminjaman - Page: {}, Size: {}", page, size);
        GetAllPeminjaman query = new GetAllPeminjaman(page, size);
        return ResponseEntity.ok(queryHandler.handle(query));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Transaksi", description = "Memperbarui data transaksi peminjaman.")
    public ResponseEntity<PeminjamanWriteModel> updatePeminjaman(
            @PathVariable("id") UUID id, 
            @Valid @RequestBody PeminjamanRequest request) {
        logger.info("API REQUEST: Update Peminjaman ID [{}]", id);
        
        UpdatePeminjamanCommand command = new UpdatePeminjamanCommand(
            id,
            UUID.fromString(request.getAnggotaId()),
            UUID.fromString(request.getBukuId()),
            request.getTanggalPinjam(),
            request.getTanggalKembali(),
            request.getStatus()
        );

        try {
            PeminjamanWriteModel updated = commandHandler.handle(command);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hapus Transaksi", description = "Menghapus data peminjaman.")
    public ResponseEntity<Void> deletePeminjaman(@PathVariable("id") UUID id) {
        logger.info("API REQUEST: Delete Peminjaman ID [{}]", id);
        DeletePeminjamanCommand command = new DeletePeminjamanCommand(id);
        try {
            commandHandler.handle(command);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update Status Cepat", description = "Hanya mengubah status (misal: DIPINJAM -> DIKEMBALIKAN).")
    public ResponseEntity<PeminjamanWriteModel> updateStatus(
            @PathVariable("id") UUID id, 
            @RequestParam String status) {
        logger.info("API REQUEST: Patch Status ID [{}] -> [{}]", id, status);
        try {
            PeminjamanWriteModel updated = commandHandler.handleUpdateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}