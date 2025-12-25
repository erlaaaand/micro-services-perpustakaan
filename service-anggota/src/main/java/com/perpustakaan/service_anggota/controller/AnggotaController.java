package com.perpustakaan.service_anggota.controller;

import com.perpustakaan.service_anggota.cqrs.command.*;
import com.perpustakaan.service_anggota.cqrs.handler.*;
import com.perpustakaan.service_anggota.cqrs.query.*;
import com.perpustakaan.service_anggota.dto.AnggotaRequest;
import com.perpustakaan.service_anggota.entity.Anggota;
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
    @Operation(summary = "Create new anggota", description = "Creates a new library member")
    public ResponseEntity<Anggota> createAnggota(@Valid @RequestBody AnggotaRequest request) {
        logger.info("Creating new anggota: {}", request.getNomorAnggota());
        
        CreateAnggotaCommand command = new CreateAnggotaCommand(
            request.getNomorAnggota(),
            request.getNama(),
            request.getAlamat(),
            request.getEmail()
        );
        
        Anggota saved = commandHandler.handle(command);
        logger.info("Successfully created anggota with ID: {}", saved.getId());
        
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get anggota by ID", description = "Retrieves a library member by their ID")
    public ResponseEntity<Anggota> getAnggotaById(@PathVariable("id") Long id) {
        logger.info("Fetching anggota with ID: {}", id);
        
        GetAnggotaByIdQuery query = new GetAnggotaByIdQuery(id);
        Anggota anggota = queryHandler.handle(query);
        
        if (anggota != null) {
            logger.info("Found anggota: {}", anggota.getNomorAnggota());
            return ResponseEntity.ok(anggota);
        }
        
        logger.warn("Anggota with ID {} not found", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    @Operation(summary = "Get all anggota", description = "Retrieves all library members with pagination")
    public ResponseEntity<Page<Anggota>> getAllAnggota(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy) {
        
        logger.info("Fetching all anggota - page: {}, size: {}", page, size);
        
        GetAllAnggotaQuery query = new GetAllAnggotaQuery(page, size, sortBy);
        Page<Anggota> anggotaPage = queryHandler.handle(query);
        
        logger.info("Found {} anggota records", anggotaPage.getTotalElements());
        return ResponseEntity.ok(anggotaPage);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update anggota", description = "Updates an existing library member")
    public ResponseEntity<Anggota> updateAnggota(
            @PathVariable("id") Long id, 
            @Valid @RequestBody AnggotaRequest request) {
        
        logger.info("Updating anggota with ID: {}", id);
        
        UpdateAnggotaCommand command = new UpdateAnggotaCommand(
            id,
            request.getNomorAnggota(),
            request.getNama(),
            request.getAlamat(),
            request.getEmail()
        );
        
        try {
            Anggota updated = commandHandler.handle(command);
            logger.info("Successfully updated anggota: {}", updated.getNomorAnggota());
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            logger.warn("Update failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete anggota", description = "Deletes a library member")
    public ResponseEntity<Void> deleteAnggota(@PathVariable("id") Long id) {
        logger.info("Deleting anggota with ID: {}", id);
        
        DeleteAnggotaCommand command = new DeleteAnggotaCommand(id);
        
        try {
            commandHandler.handle(command);
            logger.info("Successfully deleted anggota with ID: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("Delete failed: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}