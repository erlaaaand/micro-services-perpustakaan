package com.perpustakaan.service_pengembalian.controller;

import com.perpustakaan.service_pengembalian.cqrs.command.*;
import com.perpustakaan.service_pengembalian.cqrs.handler.*;
import com.perpustakaan.service_pengembalian.cqrs.query.*;
import com.perpustakaan.service_pengembalian.dto.PengembalianRequest;
import com.perpustakaan.service_pengembalian.entity.command.Pengembalian;
import com.perpustakaan.service_pengembalian.entity.query.PengembalianReadModel;
import com.perpustakaan.service_pengembalian.vo.ResponseTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/pengembalian")
@Tag(name = "Pengembalian Management", description = "APIs for managing book returns")
@RequiredArgsConstructor
public class PengembalianController {

    private final PengembalianCommandHandler commandHandler;
    private final PengembalianQueryHandler queryHandler;

    @PostMapping
    @Operation(summary = "Create Pengembalian")
    public ResponseEntity<Pengembalian> createPengembalian(@Valid @RequestBody PengembalianRequest request) {
        CreatePengembalianCommand command = new CreatePengembalianCommand(
            UUID.fromString(request.getPeminjamanId()), // Convert String request to UUID
            request.getTanggalDikembalikan(),
            request.getTerlambat(),
            request.getDenda()
        );
        Pengembalian saved = commandHandler.handle(command);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Pengembalian By ID")
    public ResponseEntity<ResponseTemplateVO> getPengembalian(@PathVariable("id") UUID id) {
        // Query menggunakan String ID (MongoDB)
        GetPengembalianByIdQuery query = new GetPengembalianByIdQuery(id.toString());
        ResponseTemplateVO response = queryHandler.handle(query);
        
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    @Operation(summary = "Get All Pengembalian")
    public ResponseEntity<Page<PengembalianReadModel>> getAllPengembalian(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        GetAllPengembalianQuery query = new GetAllPengembalianQuery(page, size);
        return ResponseEntity.ok(queryHandler.handle(query));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Pengembalian")
    public ResponseEntity<Pengembalian> updatePengembalian(
            @PathVariable("id") UUID id, 
            @Valid @RequestBody PengembalianRequest request) {
        
        UpdatePengembalianCommand command = new UpdatePengembalianCommand(
            id,
            UUID.fromString(request.getPeminjamanId()),
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
    @Operation(summary = "Delete Pengembalian")
    public ResponseEntity<Void> deletePengembalian(@PathVariable("id") UUID id) {
        DeletePengembalianCommand command = new DeletePengembalianCommand(id);
        try {
            commandHandler.handle(command);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}