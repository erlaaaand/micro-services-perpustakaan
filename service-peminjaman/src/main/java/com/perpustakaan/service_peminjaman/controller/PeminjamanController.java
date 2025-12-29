package com.perpustakaan.service_peminjaman.controller;

import com.perpustakaan.service_peminjaman.cqrs.command.*;
import com.perpustakaan.service_peminjaman.cqrs.handler.*;
import com.perpustakaan.service_peminjaman.cqrs.query.*;
import com.perpustakaan.service_peminjaman.dto.PeminjamanRequest;
import com.perpustakaan.service_peminjaman.entity.command.Peminjaman;
import com.perpustakaan.service_peminjaman.vo.ResponseTemplateVO;
import com.perpustakaan.service_peminjaman.entity.query.PeminjamanReadModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/peminjaman")
@Tag(name = "Peminjaman Management", description = "APIs for managing borrowing transactions")
public class PeminjamanController {
    
    private static final Logger logger = LoggerFactory.getLogger(PeminjamanController.class);

    @Autowired
    private PeminjamanCommandHandler commandHandler;

    @Autowired
    private PeminjamanQueryHandler queryHandler;

    @PostMapping
    @Operation(summary = "Create Peminjaman", description = "Create a new borrowing transaction")
    public ResponseEntity<Peminjaman> savePeminjaman(@Valid @RequestBody PeminjamanRequest request) {
        logger.info("Creating peminjaman for Anggota ID: {}", request.getAnggotaId());
        
        CreatePeminjamanCommand command = new CreatePeminjamanCommand(
            request.getAnggotaId(),
            request.getBukuId(),
            request.getTanggalPinjam(),
            request.getTanggalKembali(),
            request.getStatus()
        );
        
        Peminjaman saved = commandHandler.handle(command);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseTemplateVO> getPeminjaman(@PathVariable("id") Long id) {
        GetPeminjamanById query = new GetPeminjamanById(id);
        ResponseTemplateVO response = queryHandler.handle(query);
        
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<Page<PeminjamanReadModel>> getAllPeminjaman(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        GetAllPeminjaman query = new GetAllPeminjaman(page, size);
        return ResponseEntity.ok(queryHandler.handle(query));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Peminjaman", description = "Update transaction data")
    public ResponseEntity<Peminjaman> updatePeminjaman(
            @PathVariable("id") Long id, 
            @Valid @RequestBody PeminjamanRequest request) {
        
        UpdatePeminjamanCommand command = new UpdatePeminjamanCommand(
            id,
            request.getAnggotaId(),
            request.getBukuId(),
            request.getTanggalPinjam(),
            request.getTanggalKembali(),
            request.getStatus()
        );

        try {
            Peminjaman updated = commandHandler.handle(command);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Peminjaman", description = "Delete transaction data")
    public ResponseEntity<Void> deletePeminjaman(@PathVariable("id") Long id) {
        DeletePeminjamanCommand command = new DeletePeminjamanCommand(id);
        try {
            commandHandler.handle(command);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update Status", description = "Update only the status of transaction")
    public ResponseEntity<Peminjaman> updateStatus(
            @PathVariable("id") Long id, 
            @RequestParam String status) {
        
        try {
            // Kita tambahkan method khusus di CommandHandler untuk ini
            Peminjaman updated = commandHandler.handleUpdateStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}