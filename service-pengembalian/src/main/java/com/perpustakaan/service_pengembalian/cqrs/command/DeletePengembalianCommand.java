package com.perpustakaan.service_pengembalian.cqrs.command;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeletePengembalianCommand {
    private UUID id;
}