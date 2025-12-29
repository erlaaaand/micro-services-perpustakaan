package com.perpustakaan.service_anggota.controller;

import com.perpustakaan.service_anggota.cqrs.command.*;
import com.perpustakaan.service_anggota.cqrs.handler.*;
import com.perpustakaan.service_anggota.cqrs.query.*;
import com.perpustakaan.service_anggota.dto.AnggotaRequest;
import com.perpustakaan.service_anggota.entity.command.Anggota; // Entity Write (Return hasil save)
import com.perpustakaan.service_anggota.entity.query.AnggotaReadModel; // Entity Read (Return hasil query)

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
@RequestMapping("/api/anggota")
@Tag(name = "Anggota Management", description = "APIs for managing library members")
public class AnggotaController {

    private static final Logger logger = LoggerFactory.getLogger(AnggotaController.class);

    @Autowired
    private AnggotaCommandHandler commandHandler;
    
    @Autowired
    private AnggotaQueryHandler queryHandler;

    @PostMapping
    @Operation(summary = "Create new anggota")
    public ResponseEntity<Anggota> createAnggota(@Valid @RequestBody AnggotaRequest request) {
        logger.info("Creating anggota: {}", request.getNomorAnggota());
        CreateAnggotaCommand command = new CreateAnggotaCommand(
            request.getNomorAnggota(), request.getNama(), request.getAlamat(), request.getEmail()
        );
        Anggota saved = commandHandler.handle(command);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get anggota by ID")
    public ResponseEntity<AnggotaReadModel> getAnggotaById(@PathVariable("id") Long id) {
        logger.info("Fetching anggota ID: {}", id);
        GetAnggotaByIdQuery query = new GetAnggotaByIdQuery(id);
        AnggotaReadModel anggota = queryHandler.handle(query);
        
        if (anggota != null) {
            return ResponseEntity.ok(anggota);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    @Operation(summary = "Get all anggota")
    public ResponseEntity<Page<AnggotaReadModel>> getAllAnggota(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy) {
        
        GetAllAnggotaQuery query = new GetAllAnggotaQuery(page, size, sortBy);
        Page<AnggotaReadModel> anggotaPage = queryHandler.handle(query);
        return ResponseEntity.ok(anggotaPage);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update anggota")
    public ResponseEntity<Anggota> updateAnggota(@PathVariable("id") Long id, @Valid @RequestBody AnggotaRequest request) {
        UpdateAnggotaCommand command = new UpdateAnggotaCommand(
            id, request.getNomorAnggota(), request.getNama(), request.getAlamat(), request.getEmail()
        );
        try {
            Anggota updated = commandHandler.handle(command);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete anggota")
    public ResponseEntity<Void> deleteAnggota(@PathVariable("id") Long id) {
        DeleteAnggotaCommand command = new DeleteAnggotaCommand(id);
        try {
            commandHandler.handle(command);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}