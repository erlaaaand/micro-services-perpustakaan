package com.perpustakaan.service_pengembalian.controller;

import com.perpustakaan.service_pengembalian.cqrs.command.*;
import com.perpustakaan.service_pengembalian.cqrs.handler.*;
import com.perpustakaan.service_pengembalian.cqrs.query.*;
import com.perpustakaan.service_pengembalian.dto.PengembalianRequest;
import com.perpustakaan.service_pengembalian.entity.Pengembalian;
import com.perpustakaan.service_pengembalian.vo.ResponseTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pengembalian")
@Tag(name = "Pengembalian Management", description = "APIs for managing book returns")
public class PengembalianController {

    @Autowired
    private PengembalianCommandHandler commandHandler;

    @Autowired
    private PengembalianQueryHandler queryHandler;

    @PostMapping
    @Operation(summary = "Create Pengembalian", description = "Record a returned book")
    public ResponseEntity<Pengembalian> createPengembalian(@Valid @RequestBody PengembalianRequest request) {
        CreatePengembalianCommand command = new CreatePengembalianCommand(
            request.getPeminjamanId(),
            request.getTanggalDikembalikan(),
            request.getTerlambat(),
            request.getDenda()
        );
        Pengembalian saved = commandHandler.handle(command);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Pengembalian By ID", description = "Get return details including loan info")
    public ResponseEntity<ResponseTemplateVO> getPengembalian(@PathVariable("id") Long id) {
        GetPengembalianByIdQuery query = new GetPengembalianByIdQuery(id);
        ResponseTemplateVO response = queryHandler.handle(query);
        
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    @Operation(summary = "Get All Pengembalian", description = "Get all return records with pagination")
    public ResponseEntity<Page<Pengembalian>> getAllPengembalian(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        GetAllPengembalianQuery query = new GetAllPengembalianQuery(page, size);
        return ResponseEntity.ok(queryHandler.handle(query));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Pengembalian", description = "Update return record")
    public ResponseEntity<Pengembalian> updatePengembalian(
            @PathVariable("id") Long id, 
            @Valid @RequestBody PengembalianRequest request) {
        
        UpdatePengembalianCommand command = new UpdatePengembalianCommand(
            id,
            request.getPeminjamanId(),
            request.getTanggalDikembalikan(),
            request.getTerlambat(),
            request.getDenda()
        );

        try {
            Pengembalian updated = commandHandler.handle(command);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Pengembalian", description = "Delete return record")
    public ResponseEntity<Void> deletePengembalian(@PathVariable("id") Long id) {
        DeletePengembalianCommand command = new DeletePengembalianCommand(id);
        try {
            commandHandler.handle(command);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}