package com.perpustakaan.service_peminjaman.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeletePeminjamanCommand {
    private Long id;
}