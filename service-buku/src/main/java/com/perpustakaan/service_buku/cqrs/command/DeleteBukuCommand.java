package com.perpustakaan.service_buku.cqrs.command;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// DeleteAnggotaCommand.java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteBukuCommand {
    private UUID id;
}
