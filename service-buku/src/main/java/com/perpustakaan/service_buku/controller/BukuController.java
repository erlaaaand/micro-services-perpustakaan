package com.perpustakaan.service_buku.controller;

import com.perpustakaan.service_buku.cqrs.command.*;
import com.perpustakaan.service_buku.cqrs.handler.*;
import com.perpustakaan.service_buku.cqrs.query.*;
import com.perpustakaan.service_buku.dto.BukuRequest;
import com.perpustakaan.service_buku.entity.command.Buku; // Entity Write (H2)
import com.perpustakaan.service_buku.entity.query.BukuReadModel; // Entity Read (Mongo) - PENTING

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
@RequestMapping("/api/buku")
@Tag(name = "Buku Management", description = "APIs for managing books")
public class BukuController {

    private static final Logger logger = LoggerFactory.getLogger(BukuController.class);

    @Autowired
    private BukuCommandHandler commandHandler;
    
    @Autowired
    private BukuQueryHandler queryHandler;

    // --- COMMAND (WRITE) Mengembalikan Entity H2 (Buku) ---

    @PostMapping
    @Operation(summary = "Create new book", description = "Creates a new book in the library")
    public ResponseEntity<Buku> createBuku(@Valid @RequestBody BukuRequest request) {
        logger.info("Creating new buku: {}", request.getKodeBuku());
        
        CreateBukuCommand command = new CreateBukuCommand(
            request.getKodeBuku(),
            request.getJudul(),
            request.getPengarang(),
            request.getPenerbit(),
            request.getTahunTerbit()
        );
        
        Buku saved = commandHandler.handle(command);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update book", description = "Updates an existing book")
    public ResponseEntity<Buku> updateBuku(
            @PathVariable("id") Long id, 
            @Valid @RequestBody BukuRequest request) {
        
        logger.info("Updating buku with ID: {}", id);
        
        UpdateBukuCommand command = new UpdateBukuCommand(
            id,
            request.getKodeBuku(),
            request.getJudul(),
            request.getPengarang(),
            request.getPenerbit(),
            request.getTahunTerbit()
        );
        
        Buku updated = commandHandler.handle(command);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete book", description = "Deletes a book")
    public ResponseEntity<Void> deleteBuku(@PathVariable("id") Long id) {
        logger.info("Deleting buku with ID: {}", id);
        
        DeleteBukuCommand command = new DeleteBukuCommand(id);
        
        commandHandler.handle(command);
        return ResponseEntity.noContent().build();
    }

    // --- QUERY (READ) Mengembalikan ReadModel Mongo (BukuReadModel) ---

    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieves a book by its ID")
    public ResponseEntity<BukuReadModel> getBukuById(@PathVariable("id") Long id) {
        logger.info("Fetching buku with ID: {}", id);
        
        GetBukuByIdQuery query = new GetBukuByIdQuery(id);
        // Handler sekarang mengembalikan BukuReadModel
        BukuReadModel buku = queryHandler.handle(query); 
        
        if (buku != null) {
            return ResponseEntity.ok(buku);
        }
        
        logger.warn("Buku with ID {} not found", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieves all books with pagination")
    public ResponseEntity<Page<BukuReadModel>> getAllBuku(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortBy) {
        
        logger.info("Fetching all buku - page: {}, size: {}", page, size);
        
        GetAllBukuQuery query = new GetAllBukuQuery(page, size, sortBy);
        // Handler sekarang mengembalikan Page<BukuReadModel>
        Page<BukuReadModel> bukuPage = queryHandler.handle(query);
        
        return ResponseEntity.ok(bukuPage);
    }
}